package xyz.dream.launcher.managers;

import javafx.scene.text.Font;

public class Fonter {
    
    private Font gilroyMedium = null;
    private Font gilroyBold = null;
    private Font gilroyExtraBold = null;
    
    public Fonter() {
        try {
            gilroyMedium = Font.loadFont(getClass().getResourceAsStream("/Fonts/Gilroy-Medium.ttf"), 18);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            gilroyBold = Font.loadFont(getClass().getResourceAsStream("/Fonts/Gilroy-Bold.ttf"), 18);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            gilroyExtraBold = Font.loadFont(getClass().getResourceAsStream("/Fonts/Gilroy-ExtraBold.ttf"), 18);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Font getGilroyMedium() {
        return gilroyMedium;
    }
    public Font getGilroyBold() {
        return gilroyBold;
    }
    public Font getGilroyExtraBold() {
        return gilroyExtraBold;
    }
}