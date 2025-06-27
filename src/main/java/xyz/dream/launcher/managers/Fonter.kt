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