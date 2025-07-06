use std::path::Path;
use tokio::process::Command;
use std::process::Stdio;

/// Configuration for the Java command to launch Minecraft.
///
/// This struct holds the JVM arguments, platform-specific arguments, and game arguments.
pub struct JavaConfig {
    jvm_args: Vec<String>,
    platform_args: Vec<String>,
    game_args: Vec<(String, String)>,
}

/// Implementation of the JavaConfig struct.
///
/// Provides methods to create a new configuration and build the command to launch Minecraft.
impl JavaConfig {
    /// Creates a new JavaConfig with default JVM, platform, and game arguments.
    ///
    /// # Returns
    /// A new instance of JavaConfig with pre-filled arguments suitable for launching Minecraft.
    pub fn new() -> Self {
        let jvm_args = vec![
            "-Xmx4G".to_string(),
            "-Xms1G".to_string(),
        ];

        // On macOS, we need to start the JVM on the first thread to avoid issues with OpenGL
        let platform_args = if cfg!(target_os = "macos") {
            vec!["-XstartOnFirstThread".to_string()]
        } else {
            vec![]
        };

        let game_args = vec![
            ("--username".to_string(), "Player".to_string()),
            ("--version".to_string(), "1.21.7".to_string()),
            ("--userType".to_string(), "mojang".to_string()),
            ("--versionType".to_string(), "release".to_string()),
            ("--uuid".to_string(), "00000000-0000-0000-0000-000000000000".to_string()),
            ("--accessToken".to_string(), "0".to_string()),
        ];

        Self {
            jvm_args,
            platform_args,
            game_args,
        }
    }

    /// Builds the command to launch Minecraft with the specified parameters.
    ///
    /// # Arguments
    /// * `classpath` - The classpath for the Minecraft application.
    /// * `main_class` - The main class to run (usually "net.minecraft.client.main.Main").
    /// * `minecraft_dir` - The directory where Minecraft is located.
    /// * `asset_index_id` - The ID of the asset index to use.
    ///
    /// # Returns
    /// A `Command` object that can be used to run the Minecraft application.
    pub fn build_command(
        &self,
        classpath: &str,
        main_class: &str,
        minecraft_dir: &Path,
        asset_index_id: &str,
    ) -> Command {
        let mut command = Command::new("java");
        command
            .args(&self.jvm_args)
            .args(&self.platform_args)
            .arg("-cp")
            .arg(classpath)
            .arg(main_class)
            .args(self.game_args.iter().flat_map(|(k, v)| vec![k.clone(), v.clone()]))
            .arg("--gameDir")
            .arg(minecraft_dir)
            .arg("--assetsDir")
            .arg(minecraft_dir.join("assets"))
            .arg("--assetIndex")
            .arg(asset_index_id)
            .stdout(Stdio::inherit())
            .stderr(Stdio::inherit());
        command
    }
}