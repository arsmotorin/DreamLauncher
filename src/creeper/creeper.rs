use std::collections::HashSet;
use std::fs;
use std::io::{self, Write};
use std::path::Path;
use std::sync::Arc;

use futures::{stream, StreamExt};
use glob::glob;
use reqwest::Client;
use tokio::fs as tokio_fs;
use tokio::process::Command;

use crate::creeper::java_config::JavaConfig;
use crate::creeper::minecraft_models::{
    AssetIndex, AssetIndexManifest, Library, VersionDetails, VersionManifest,
};
use crate::creeper::progress_bar::ProgressBar;

/// Downloads a file from the given URL if it does not already exist at the specified path.
/// Optionally verifies the file size if `expected_size` is provided.
/// Updates the provided `progress_bar` if present.
///
/// # Arguments
/// * `client` - The HTTP client to use for downloading.
/// * `url` - The URL of the file to download.
/// * `path` - The local path where the file should be saved.
/// * `expected_size` - Optional expected file size for verification.
/// * `progress_bar` - Optional progress bar to update.
///
/// # Errors
/// Returns an error if the download fails, the file size does not match, or writing to disk fails.
async fn download_file_if_not_exists(
    client: &Client,
    url: &str,
    path: &Path,
    expected_size: Option<u64>,
    progress_bar: Option<&ProgressBar>,
) -> Result<(), Box<dyn std::error::Error>> {
    if tokio_fs::metadata(path).await.is_ok() {
        if let Some(pb) = progress_bar {
            pb.increment();
        }
        return Ok(());
    }

    if let Some(parent) = path.parent() {
        tokio_fs::create_dir_all(parent).await?;
    }

    let response = client.get(url).send().await;
    if let Err(e) = response {
        return Err(format!("Failed to download {}: {}", url, e).into());
    }
    let response = response?;
    if !response.status().is_success() {
        return Err(format!("Failed to download {}: HTTP {}", url, response.status()).into());
    }

    let data = response.bytes().await?;
    if let Some(size) = expected_size {
        if data.len() as u64 != size {
            return Err(format!("Size mismatch for {}: expected {}, got {}", url, size, data.len()).into());
        }
    }

    tokio_fs::write(path, &data).await?;
    if let Some(pb) = progress_bar {
        pb.increment();
    }
    Ok(())
}

/// Downloads and caches all Minecraft assets as specified in the given `asset_index`.
/// Downloads the asset index manifest if not already cached, and then downloads all unique asset objects in parallel.
///
/// # Arguments
/// * `client` - The HTTP client to use for downloading.
/// * `asset_index` - The asset index information.
/// * `minecraft_dir` - The root directory of the Minecraft installation.
///
/// # Errors
/// Returns an error if any asset or the index manifest fails to download or parse.
async fn download_assets(
    client: &Client,
    asset_index: &AssetIndex,
    minecraft_dir: &Path,
) -> Result<(), Box<dyn std::error::Error>> {
    println!("Downloading asset index from {}", asset_index.url);
    let indexes_dir = minecraft_dir.join("assets/indexes");
    tokio_fs::create_dir_all(&indexes_dir).await?;
    let index_path = indexes_dir.join(format!("{}.json", asset_index.id));

    let asset_index_manifest: AssetIndexManifest = if index_path.exists() {
        println!("Using cached asset index: {}", index_path.display());
        serde_json::from_str(&tokio_fs::read_to_string(&index_path).await?).map_err(|e| format!("Failed to parse asset index: {}", e))?
    } else {
        let response = client.get(&asset_index.url).send().await;
        if let Err(e) = response {
            return Err(format!("Failed to fetch asset index: {}", e).into());
        }
        let response = response?;
        if !response.status().is_success() {
            return Err(format!("Failed to fetch asset index: HTTP {}", response.status()).into());
        }
        let manifest: AssetIndexManifest = response.json().await.map_err(|e| format!("Failed to parse asset index JSON: {}", e))?;
        tokio_fs::write(&index_path, serde_json::to_string(&manifest)?).await?;
        println!("Asset index saved to {}", index_path.display());
        manifest
    };

    let assets_objects_dir = minecraft_dir.join("assets/objects");
    tokio_fs::create_dir_all(&assets_objects_dir).await?;

    let unique_assets: Vec<_> = {
        let mut downloaded_hashes = HashSet::new();
        asset_index_manifest
            .objects
            .iter()
            .filter(|(_, asset)| downloaded_hashes.insert(asset.hash.clone()))
            .collect()
    };

    let total_assets = unique_assets.len();
    println!("Downloading {} assets", total_assets);
    let progress_bar = Arc::new(ProgressBar::new(total_assets, "Downloading assets".to_string()));
    tokio::spawn({
        let progress_bar = progress_bar.clone();
        async move { progress_bar.start_periodic_update().await }
    });

    let results: Vec<_> = stream::iter(unique_assets)
        .map(|(_path, asset)| {
            let client = client.clone();
            let assets_objects_dir = assets_objects_dir.clone();
            let hash = asset.hash.clone();
            let size = asset.size;
            let progress_bar = progress_bar.clone();

            async move {
                let subdir = &hash[0..2];
                let file_path = assets_objects_dir.join(subdir).join(&hash);
                let url = format!("https://resources.download.minecraft.net/{}/{}", subdir, hash);
                download_file_if_not_exists(&client, &url, &file_path, Some(size), Some(&progress_bar))
                    .await
                    .map_err(|e| format!("Failed to download asset {}: {}", url, e))
            }
        })
        .buffer_unordered(32)
        .collect()
        .await;

    let errors: Vec<_> = results
        .into_iter()
        .filter_map(|r| r.err())
        .collect();
    if !errors.is_empty() {
        for error in &errors {
            eprintln!("Error: {}", error);
        }
        eprintln!("Warning: {} assets failed to download", errors.len());
    } else {
        println!("\nAll assets downloaded successfully");
    }
    Ok(())
}

/// Downloads all required Minecraft libraries in parallel.
/// Only downloads libraries that have a valid artifact download entry.
///
/// # Arguments
/// * `client` - The HTTP client to use for downloading.
/// * `libraries` - The list of libraries to download.
/// * `libraries_dir` - The directory where libraries should be stored.
///
/// # Errors
/// Returns an error if any library fails to download.
async fn download_libraries(
    client: &Client,
    libraries: &[Library],
    libraries_dir: &Path,
) -> Result<(), Box<dyn std::error::Error>> {
    let valid_libs: Vec<_> = libraries
        .iter()
        .filter_map(|lib| lib.downloads.as_ref().and_then(|d| d.artifact.as_ref()))
        .collect();

    if valid_libs.is_empty() {
        return Ok(());
    }

    println!("Downloading {} libraries", valid_libs.len());
    let progress_bar = Arc::new(ProgressBar::new(valid_libs.len(), "Downloading libraries".to_string()));
    tokio::spawn({
        let progress_bar = progress_bar.clone();
        async move { progress_bar.start_periodic_update().await }
    });

    let results: Vec<_> = stream::iter(valid_libs)
        .map(|artifact| {
            let client = client.clone();
            let path = libraries_dir.join(&artifact.path);
            let progress_bar = progress_bar.clone();
            async move {
                download_file_if_not_exists(&client, &artifact.url, &path, None, Some(&progress_bar))
                    .await
                    .map_err(|e| format!("Failed to download library {}: {}", artifact.url, e))
            }
        })
        .buffer_unordered(32)
        .collect()
        .await;

    let errors: Vec<_> = results
        .into_iter()
        .filter_map(|r| r.err())
        .collect();
    if !errors.is_empty() {
        for error in &errors {
            eprintln!("Error: {}", error);
        }
        eprintln!("Warning: {} libraries failed to download", errors.len());
    } else {
        println!("\nAll libraries downloaded successfully");
    }
    Ok(())
}

/// Ensures that all necessary Minecraft directories exist, creating them if needed.
///
/// # Arguments
/// * `minecraft_dir` - The root directory of the Minecraft installation.
///
/// # Errors
/// Returns an error if any directory cannot be created.
fn ensure_minecraft_directory(minecraft_dir: &Path) -> Result<(), Box<dyn std::error::Error>> {
    for subdir in ["libraries", "versions", "assets/objects", "assets/indexes"] {
        fs::create_dir_all(minecraft_dir.join(subdir))?;
    }
    Ok(())
}

/// Builds the Java classpath string by collecting all JAR files in the libraries directory and appending the client JAR.
///
/// # Arguments
/// * `libraries_dir` - The directory containing library JARs.
/// * `client_jar_path` - The path to the Minecraft client JAR.
///
/// # Errors
/// Returns an error if the classpath cannot be constructed.
fn build_classpath(libraries_dir: &Path, client_jar_path: &Path) -> Result<String, Box<dyn std::error::Error>> {
    let mut classpath = String::new();
    let pattern = format!("{}/**/*.jar", libraries_dir.display());
    for entry in glob(&pattern)? {
        classpath.push_str(&format!("{}:", entry?.display()));
    }
    classpath.push_str(client_jar_path.to_str().ok_or("Invalid client jar path")?);
    Ok(classpath)
}

/// The main entry point for the launcher.
/// Handles user input and launches Minecraft as requested.
///
/// # Errors
/// Returns an error if any operation fails.
#[tokio::main]
pub async fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("Launcher by cubelius\nCommands: start, exit");

    let client = Client::builder()
        .pool_max_idle_per_host(16)
        .timeout(std::time::Duration::from_secs(90))
        .build()?;
    let minecraft_dir = Path::new(".minecraft");
    ensure_minecraft_directory(&minecraft_dir)?;

    loop {
        print!("\nEnter command: ");
        io::stdout().flush()?;
        let mut input = String::new();
        io::stdin().read_line(&mut input)?;
        match input.trim() {
            "exit" => break,
            "start" => {
                if let Err(e) = start_minecraft(&client, &minecraft_dir).await {
                    eprintln!("Failed to start Minecraft: {}", e);
                } else {
                    println!("Minecraft launched successfully");
                }
            }
            cmd => println!("Unknown command: '{}'. Available: start, exit", cmd),
        }
    }
    Ok(())
}

/// Downloads all required files and launches Minecraft for the specified version.
///
/// # Arguments
/// * `client` - The HTTP client to use for downloading.
/// * `minecraft_dir` - The root directory of the Minecraft installation.
///
/// # Errors
/// Returns an error if any step in the process fails.
async fn start_minecraft(client: &Client, minecraft_dir: &Path) -> Result<(), Box<dyn std::error::Error>> {
    let version = "1.21.7";
    let manifest_url = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    println!("Fetching version manifest from {}", manifest_url);
    let response = client.get(manifest_url).send().await;
    if let Err(e) = response {
        return Err(format!("Failed to fetch version manifest: {}", e).into());
    }
    let manifest: VersionManifest = response?.json().await?;
    println!("Version manifest fetched");

    let version_info = manifest
        .versions
        .iter()
        .find(|v| v.id == version)
        .ok_or(format!("Version {} not found", version))?;
    println!("Found version {}", version);

    println!("Fetching version details from {}", version_info.url);
    let version_details: VersionDetails = client.get(&version_info.url).send().await?.json().await?;
    println!("Version details fetched");

    let versions_dir = minecraft_dir.join("versions");
    let libraries_dir = minecraft_dir.join("libraries");
    let client_jar_path = versions_dir.join(format!("{}.jar", version));

    if !client_jar_path.exists() {
        println!("Downloading Minecraft client from {}", version_details.downloads.client.url);
        download_file_if_not_exists(client, &version_details.downloads.client.url, &client_jar_path, None, None).await?;
        println!("Client downloaded");
    } else {
        println!("Client already exists at {}", client_jar_path.display());
    }

    download_libraries(client, &version_details.libraries, &libraries_dir).await?;
    download_assets(client, &version_details.asset_index, minecraft_dir).await?;

    println!("Building classpath...");
    let classpath = build_classpath(&libraries_dir, &client_jar_path)?;

    let java_version = Command::new("java").arg("-version").output().await?;
    println!("Java version: {:?}", String::from_utf8_lossy(&java_version.stderr));

    println!("Starting Minecraft...");
    let java_config = JavaConfig::new();
    let mut command = java_config.build_command(
        &classpath,
        &version_details.main_class,
        minecraft_dir,
        &version_details.asset_index.id,
    );

    println!("Java command: {:?}", command);
    let status = command.spawn()?.wait().await?;
    println!("Minecraft exited with code: {}", status.code().unwrap_or(-1));
    Ok(())
}