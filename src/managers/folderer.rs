// Copyright 2025 © Arsenii Motorin
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

use std::fs;

// Function that returns the path to the DreamLauncher folder based on the operating system
pub fn folderer() -> String {
    // cfg! is a Rust attribute that allows conditional compilation based on the target operating system
    // It checks if the target OS is macOS, Windows, or any other OS and returns
    // cfg! returns a boolean value indicating whether the condition is true or false
    let folder_path = if cfg!(target_os = "macos") {

        // unwrap() is used to get the value inside an option or result
        // If the value is None or Err, it will panic
        dirs::home_dir().unwrap().join("Library/Application Support/DreamLauncher")
    } else if cfg!(target_os = "windows") {
        dirs::data_dir().unwrap().join("DreamLauncher")
    } else {
        dirs::home_dir().unwrap().join("DreamLauncher")
    };

    // Error handling for creating the directory
    if let Err(e) = fs::create_dir_all(&folder_path) {
        eprintln!("Error creating DreamLauncher directory: {}", e);
        } else {
        println!("DreamLauncher folder created at: {}", folder_path.display());
    }
    
    // Return
    folder_path.to_string_lossy().to_string()
}