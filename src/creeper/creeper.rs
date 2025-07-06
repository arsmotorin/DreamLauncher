use std::io::{self, Write};
use std::path::Path;
use std::sync::Arc;

use reqwest::Client;
use tokio::process::Command;

use crate::creeper::java_config::JavaConfig;
use crate::creeper::minecraft_models::{
    AssetIndex, Library, VersionDetails, VersionManifest,
};
use crate::creeper::progress_bar::ProgressBar;
use crate::creeper::downloader::Downloader;
use crate::creeper::filesystem::FileSystem;

/// The main entry point for the launcher.
/// Handles user input and launches Minecraft as requested.
///
/// # Errors
/// Returns an error if any operation fails.
#[tokio::main]
pub async fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("Launcher by cubelius\nCommands: boom, exit");

    let client = Client::builder()
        .pool_max_idle_per_host(16)
        .timeout(std::time::Duration::from_secs(90))
        .build()?;
    let minecraft_dir = Path::new(".minecraft");
    FileSystem::ensure_minecraft_directory(&minecraft_dir)?;

    let downloader = Downloader::new(client.clone());
    let fs = FileSystem::new();

    loop {
        print!("\nEnter command: ");
        io::stdout().flush()?;
        let mut input = String::new();
        io::stdin().read_line(&mut input)?;
        match input.trim() {
            "exit" => break,
            "boom" => {
                if let Err(e) = start_minecraft(&downloader, &fs, &minecraft_dir).await {
                    eprintln!("Failed to start Minecraft: {}", e);
                } else {
                    println!("Minecraft launched successfully");
                }
            }
            cmd => println!("Unknown command: '{}'. Available: boom, exit", cmd),
        }
    }
    Ok(())
}

/// Downloads all required files and launches Minecraft for the specified version.
///
/// # Arguments
/// * `downloader` - Downloader instance for downloading files.
/// * `fs` - FileSystem instance for file operations.
/// * `minecraft_dir` - The root directory of the Minecraft installation.
///
/// # Errors
/// Returns an error if any step in the process fails.
async fn start_minecraft(
    downloader: &Downloader,
    fs: &FileSystem,
    minecraft_dir: &Path,
) -> Result<(), Box<dyn std::error::Error>> {
    let version = "1.21.7";
    let manifest_url = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    println!("Fetching version manifest from {}", manifest_url);
    let manifest: VersionManifest = downloader.get_json(manifest_url).await?;
    println!("Version manifest fetched");

    let version_info = manifest
        .versions
        .iter()
        .find(|v| v.id == version)
        .ok_or(format!("Version {} not found", version))?;
    println!("Found version {}", version);

    println!("Fetching version details from {}", version_info.url);
    let version_details: VersionDetails = downloader.get_json(&version_info.url).await?;
    println!("Version details fetched");

    let versions_dir = minecraft_dir.join("versions");
    let libraries_dir = minecraft_dir.join("libraries");
    let client_jar_path = versions_dir.join(format!("{}.jar", version));

    if !fs.exists(&client_jar_path) {
        println!("Downloading Minecraft client from {}", version_details.downloads.client.url);
        downloader
            .download_file_if_not_exists(
                &version_details.downloads.client.url,
                &client_jar_path,
                None,
                None,
            )
            .await?;
        println!("Client downloaded");
    } else {
        println!("Client already exists at {}", client_jar_path.display());
    }

    downloader
        .download_libraries(&version_details.libraries, &libraries_dir)
        .await?;
    downloader
        .download_assets(&version_details.asset_index, minecraft_dir)
        .await?;

    println!("Building classpath...");
    let classpath = fs.build_classpath(&libraries_dir, &client_jar_path)?;

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