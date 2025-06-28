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

package xyz.dream.launcher.managers

import javafx.scene.text.Font

/**
 * Initializes the Fonter class by loading the Gilroy fonts.
 * The fonts are loaded from resources and set to the properties.
 */
class Fonter {
    var gilroyMedium: Font? = null
        private set
    var gilroyBold: Font? = null
        private set
    var gilroyExtraBold: Font? = null
        private set

    init {
        try {
            gilroyMedium = Font.loadFont(javaClass.getResourceAsStream("/Fonts/Gilroy-Medium.ttf"), 18.0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            gilroyBold = Font.loadFont(javaClass.getResourceAsStream("/Fonts/Gilroy-Bold.ttf"), 18.0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            gilroyExtraBold = Font.loadFont(javaClass.getResourceAsStream("/Fonts/Gilroy-ExtraBold.ttf"), 18.0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}