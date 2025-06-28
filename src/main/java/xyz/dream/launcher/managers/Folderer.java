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

package xyz.dream.launcher.managers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Folderer {

    /**
     * Creates a folder for Minecraft files.
     * The folder is created in the user's home directory or the appropriate application data directory.
     *
     * @return The path to the created folder as a String.
     */
    public static String createTheFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        String folderPath;
        
        if (os.contains("mac")) {
            folderPath = userHome + "/Library/Application Support/DreamLauncher";
        } else if (os.contains("win")) {
            folderPath = System.getenv("APPDATA") + "\\DreamLauncher";
        } else {
            folderPath = userHome + "/DreamLauncher";
        }
        
        try {
            Path path = Paths.get(folderPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("DreamLauncher folder created at: " + folderPath);
            }
        } catch (Exception e) {
            System.err.println("Error creating DreamLauncher directory: " + e.getMessage());
        }
        
        return folderPath;
    }
}
