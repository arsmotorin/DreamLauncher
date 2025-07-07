use std::io::{self, Write};
use std::path::Path;

use tokio::process::Command;
use tokio::try_join;

use crate::creeper::java_config::JavaConfig;
use crate::creeper::minecraft_models::{VersionDetails, VersionManifest};
use crate::creeper::downloader::Downloader;
use crate::creeper::filesystem::FileSystem;

/// CLI Launcher for Minecraft.
#[tokio::main]
pub async fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("Launcher by cubelius\nCommands: boom, exit");

    let downloader = Downloader::new();
    let minecraft_dir = Path::new(".minecraft");
    FileSystem::ensure_minecraft_directory(&minecraft_dir)?;

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
                }
            }
            cmd => println!("Unknown command: '{}'. Available: boom, exit", cmd),
        }
    }
    Ok(())
}

/// Starts Minecraft by downloading the necessary files and launching the game.
///
/// # Arguments
/// * `downloader` - Handles file downloads.
/// * `fs` - Manages filesystem operations.
/// * `minecraft_dir` - Path to the .minecraft directory.
///
/// # Returns
/// Result indicating success or failure.
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

    let client_jar_fut = if !fs.exists(&client_jar_path) {
        Some(downloader.download_file_if_not_exists(
            &version_details.downloads.client.url,
            &client_jar_path,
            None,
            None,
        ))
    } else {
        println!("Client already exists at {}", client_jar_path.display());
        None
    };

    let libs_fut = downloader.download_libraries(&version_details.libraries, &libraries_dir);
    let assets_fut = downloader.download_assets(&version_details.asset_index, minecraft_dir);

    if let Some(fut) = client_jar_fut {
        try_join!(fut, libs_fut, assets_fut)?;
        println!("Client downloaded");
    } else {
        try_join!(libs_fut, assets_fut)?;
    }

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