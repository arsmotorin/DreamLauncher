use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// Minecraft models for handling version manifests, version details, and asset indices.
/// These structs are used to deserialize JSON data from the Minecraft API.
#[derive(Deserialize, Serialize)]
pub struct VersionManifest {
    pub versions: Vec<VersionInfo>,
}

#[derive(Deserialize, Serialize)]
pub struct VersionInfo {
    pub id: String,
    pub url: String,
}

#[derive(Deserialize)]
pub struct VersionDetails {
    pub downloads: Downloads,
    pub libraries: Vec<Library>,
    #[serde(rename = "mainClass")]
    pub main_class: String,
    #[serde(rename = "assetIndex")]
    pub asset_index: AssetIndex,
}

#[derive(Deserialize)]
pub struct Downloads {
    pub client: DownloadInfo,
}

#[derive(Deserialize)]
pub struct DownloadInfo {
    pub url: String,
}

#[derive(Deserialize)]
pub struct AssetIndex {
    pub id: String,
    pub url: String,
}

#[derive(Deserialize)]
pub struct Library {
    #[allow(dead_code)]
    pub name: String,
    pub downloads: Option<LibraryDownloads>,
    pub natives: Option<HashMap<String, String>>,
    pub extract: Option<ExtractRule>,
    pub rules: Option<Vec<Rule>>,
}

#[derive(Deserialize)]
pub struct LibraryDownloads {
    pub artifact: Option<Artifact>,
    pub classifiers: Option<HashMap<String, Artifact>>,
}

#[derive(Deserialize)]
pub struct Artifact {
    pub url: String,
    pub path: String,
}

#[derive(Deserialize, Serialize)]
pub struct AssetIndexManifest {
    pub objects: HashMap<String, AssetObject>,
}

#[derive(Deserialize, Serialize)]
pub struct AssetObject {
    pub hash: String,
    pub size: u64,
}

#[derive(Deserialize)]
pub struct ExtractRule {
    pub exclude: Option<Vec<String>>,
}

#[derive(Deserialize)]
pub struct Rule {
    pub action: String,
    pub os: Option<OsRule>,
}

#[derive(Deserialize)]
pub struct OsRule {
    #[allow(dead_code)]
    pub name: Option<String>,
}
