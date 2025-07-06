use std::collections::HashSet;
use std::path::{Path};
use std::sync::Arc;

use futures::{stream, StreamExt};
use reqwest::Client;
use tokio::fs as tokio_fs;

use crate::creeper::minecraft_models::{AssetIndex, AssetIndexManifest, Library};
use crate::creeper::progress_bar::ProgressBar;

pub struct Downloader {
    client: Client,
}

impl Downloader {
    pub fn new(client: Client) -> Self {
        Self { client }
    }

    pub async fn get_json<T: serde::de::DeserializeOwned>(
        &self,
        url: &str,
    ) -> Result<T, Box<dyn std::error::Error>> {
        let response = self.client.get(url).send().await?;
        if !response.status().is_success() {
            return Err(format!("Failed to fetch {}: HTTP {}", url, response.status()).into());
        }
        Ok(response.json().await?)
    }

    pub async fn download_file_if_not_exists(
        &self,
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

        let response = self.client.get(url).send().await;
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
                return Err(format!(
                    "Size mismatch for {}: expected {}, got {}",
                    url, size, data.len()
                )
                    .into());
            }
        }

        tokio_fs::write(path, &data).await?;
        if let Some(pb) = progress_bar {
            pb.increment();
        }
        Ok(())
    }

    pub async fn download_assets(
        &self,
        asset_index: &AssetIndex,
        minecraft_dir: &Path,
    ) -> Result<(), Box<dyn std::error::Error>> {
        println!("Downloading asset index from {}", asset_index.url);
        let indexes_dir = minecraft_dir.join("assets/indexes");
        tokio_fs::create_dir_all(&indexes_dir).await?;
        let index_path = indexes_dir.join(format!("{}.json", asset_index.id));

        let asset_index_manifest: AssetIndexManifest = if index_path.exists() {
            println!("Using cached asset index: {}", index_path.display());
            serde_json::from_str(&tokio_fs::read_to_string(&index_path).await?)
                .map_err(|e| format!("Failed to parse asset index: {}", e))?
        } else {
            let response = self.client.get(&asset_index.url).send().await;
            if let Err(e) = response {
                return Err(format!("Failed to fetch asset index: {}", e).into());
            }
            let response = response?;
            if !response.status().is_success() {
                return Err(format!(
                    "Failed to fetch asset index: HTTP {}",
                    response.status()
                )
                    .into());
            }
            let manifest: AssetIndexManifest = response
                .json()
                .await
                .map_err(|e| format!("Failed to parse asset index JSON: {}", e))?;
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
        let progress_bar = Arc::new(ProgressBar::new(
            total_assets,
            "Downloading assets".to_string(),
        ));
        tokio::spawn({
            let progress_bar = progress_bar.clone();
            async move { progress_bar.start_periodic_update().await }
        });

        let results: Vec<_> = stream::iter(unique_assets)
            .map(|(_path, asset)| {
                let client = self.client.clone();
                let assets_objects_dir = assets_objects_dir.clone();
                let hash = asset.hash.clone();
                let size = asset.size;
                let progress_bar = progress_bar.clone();

                async move {
                    let subdir = &hash[0..2];
                    let file_path = assets_objects_dir.join(subdir).join(&hash);
                    let url =
                        format!("https://resources.download.minecraft.net/{}/{}", subdir, hash);
                    Downloader { client }
                        .download_file_if_not_exists(
                            &url,
                            &file_path,
                            Some(size),
                            Some(&progress_bar),
                        )
                        .await
                        .map_err(|e| format!("Failed to download asset {}: {}", url, e))
                }
            })
            .buffer_unordered(32)
            .collect()
            .await;

        let errors: Vec<_> = results.into_iter().filter_map(|r| r.err()).collect();
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

    pub async fn download_libraries(
        &self,
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
        let progress_bar = Arc::new(ProgressBar::new(
            valid_libs.len(),
            "Downloading libraries".to_string(),
        ));
        tokio::spawn({
            let progress_bar = progress_bar.clone();
            async move { progress_bar.start_periodic_update().await }
        });

        let results: Vec<_> = stream::iter(valid_libs)
            .map(|artifact| {
                let client = self.client.clone();
                let path = libraries_dir.join(&artifact.path);
                let progress_bar = progress_bar.clone();
                async move {
                    Downloader { client }
                        .download_file_if_not_exists(
                            &artifact.url,
                            &path,
                            None,
                            Some(&progress_bar),
                        )
                        .await
                        .map_err(|e| format!("Failed to download library {}: {}", artifact.url, e))
                }
            })
            .buffer_unordered(48)
            .collect()
            .await;

        let errors: Vec<_> = results.into_iter().filter_map(|r| r.err()).collect();
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
}