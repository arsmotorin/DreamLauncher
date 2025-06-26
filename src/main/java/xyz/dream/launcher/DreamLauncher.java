package xyz.dream.launcher;

import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class DreamLauncher extends JFrame {
    static boolean isTextChanged = false;
    private static JLabel enterLabel;
    static JTextField nickname;
    static DreamLauncher display;
    static String folderPath;
    static Config config;

    static {
        folderPath = System.getenv("LOCALAPPDATA") + "/FrogDreamCache";
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            folderPath = System.getProperty("user.home") + "/Library/FrogDreamCache";
        }
    }

    public static class Config {
        public String nickName;
    }

    public DreamLauncher() {
        initializeWindow();
    }

    private void initializeWindow() {
        ImageIcon logo = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/logo.png")));
        JLabel logoLabel = new JLabel(logo);
        Dimension size = logoLabel.getPreferredSize();
        logoLabel.setBounds(300, 203, size.width, size.height);
        add(logoLabel);
    }

    private void center() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

    public static Config loadConfig() {
        String filePath = folderPath + "/autofill.json";
        try {
            return new Gson().fromJson(Files.readString(Path.of(filePath)), Config.class);
        } catch (IOException e) {
            return new Config();
        }
    }

    public static void saveConfig(Config config) {
        String filePath = folderPath + "/autofill.json";
        try {
            Files.writeString(Path.of(filePath), new Gson().toJson(config));
        } catch (IOException e) {
            System.err.println("Failed to save config file.");
        }
    }

    public static void main(String[] args) {
        display = new DreamLauncher();
        ensureFolderExists();
        config = loadConfig();

        if (config.nickName != null) {
            System.out.println("Auto-fill is successful, changing screen to MainScreen...");
            changeToMainScreen(config.nickName);
        } else {
            System.out.println("Error with auto-fill, changing screen to FrogdreamLauncher...");
            setupLauncherScreen();
        }
    }

    private static void ensureFolderExists() {
        File folder = new File(folderPath);
        if (!folder.exists() && !folder.mkdirs()) {
            System.err.println("Failed to create cache folder.");
            System.exit(1);
        }
    }

    private static void changeToMainScreen(String enteredNickname) {
        MainScreen mainscreen = new MainScreen();
        MainScreen.MainScreenInitializer.initialize(mainscreen, enteredNickname);
        display.setVisible(false);
        display.dispose();
    }

    private static void setupLauncherScreen() {
        display.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        display.setTitle("Frogdream Launcher");
        display.setSize(1022, 600);
        display.getContentPane().setBackground(new Color(12, 12, 12));
        display.setLayout(null);

        JLabel launcherText = createLauncherLabel();
        display.add(launcherText);

        nickname = createNicknameField();
        display.add(nickname);

        JLabel rectangleLabel = createRectangleLabel();
        display.add(rectangleLabel);

        enterLabel = createEnterLabel();
        display.add(enterLabel);

        display.center();
        display.setResizable(false);
        display.setVisible(true);
    }

    private static JLabel createLauncherLabel() {
        JLabel launcherText = new JLabel("Launcher");
        launcherText.setForeground(new Color(116, 168, 50));

        try (InputStream is = DreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Bold.ttf")) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is));
            launcherText.setFont(font.deriveFont(56.0F));
            launcherText.setBounds(478, -19, 2000, 600);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException(e);
        }

        return launcherText;
    }

    private static JTextField createNicknameField() {
        JTextField nicknameField = new JTextField("Nickname");
        nicknameField.setBorder(null);
        nicknameField.setOpaque(false);
        nicknameField.setForeground(new Color(100, 101, 101));
        nicknameField.setBounds(315, 375, 385, 60);
        nicknameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isTextChanged) {
                    nicknameField.setText("");
                    isTextChanged = true;
                    nicknameField.setForeground(new Color(100, 101, 101));
                }
            }
        });

        try (InputStream fontStream = DreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Medium.ttf")) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(fontStream));
            nicknameField.setFont(font.deriveFont(16.0F));
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException(e);
        }

        nicknameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performAction();
                }
            }
        });

        return nicknameField;
    }

    private static JLabel createRectangleLabel() {
        ImageIcon rectangle = new ImageIcon(Objects.requireNonNull(DreamLauncher.class.getResource("/Images/rectangle.png")));
        JLabel rectangleLabel = new JLabel(rectangle);
        Dimension size = rectangleLabel.getPreferredSize();
        rectangleLabel.setBounds(293, 380, size.width, size.height);
        return rectangleLabel;
    }

    private static JLabel createEnterLabel() {
        final ImageIcon enter = new ImageIcon(Objects.requireNonNull(DreamLauncher.class.getResource("/Images/enter.png")));
        JLabel enterLabel = new JLabel(enter);
        enterLabel.setBounds(704, 390, 30, 30);
        enterLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                enterLabel.setIcon(getBrighterIcon(enter));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                enterLabel.setIcon(enter);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                performAction();
            }
        });
        return enterLabel;
    }

    private static void performAction() {
        String enteredNickname = nickname.getText();

        if (isValidNickname(enteredNickname)) {
            changeToMainScreen(enteredNickname);
            config.nickName = enteredNickname;
            saveConfig(config);
        } else {
            nickname.setText("Incorrect nickname!");
            nickname.setForeground(new Color(222, 45, 56));
        }
    }

    private static boolean isValidNickname(String nickname) {
        return nickname.matches("[a-zA-Z0-9_-]+");
    }

    private static ImageIcon getBrighterIcon(ImageIcon icon) {
        Image img = icon.getImage();
        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(img, 0, 0, null);
        graphics.dispose();

        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int rgb = bufferedImage.getRGB(x, y);
                Color color = new Color(rgb, true);
                int r = Math.min((int) (color.getRed() * 1.4), 255);
                int g = Math.min((int) (color.getGreen() * 1.4), 255);
                int b = Math.min((int) (color.getBlue() * 1.4), 255);
                bufferedImage.setRGB(x, y, new Color(r, g, b, color.getAlpha()).getRGB());
            }
        }

        return new ImageIcon(bufferedImage);
    }
}
