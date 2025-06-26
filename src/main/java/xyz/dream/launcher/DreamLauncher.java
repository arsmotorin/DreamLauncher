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
import java.util.concurrent.atomic.AtomicBoolean;

public class DreamLauncher extends JFrame {
    private static final AtomicBoolean isTextChanged = new AtomicBoolean(false);
    private static JLabel enterLabel;
    private static JTextField nickname;
    private static DreamLauncher display;
    private static String folderPath;
    private static Config config;
    private static final Color NICKNAME_COLOR = new Color(100, 101, 101);
    private static final Color ERROR_COLOR = new Color(222, 45, 56);
    private static final Color LAUNCHER_COLOR = new Color(116, 168, 50);
    private static final Color BACKGROUND_COLOR = new Color(12, 12, 12);
    private static final String DEFAULT_NICKNAME_TEXT = "Nickname";
    private static final String ERROR_NICKNAME_TEXT = "Incorrect nickname!";
    private static final int WINDOW_WIDTH = 1022;
    private static final int WINDOW_HEIGHT = 600;

    static {
        folderPath = System.getProperty("os.name").toLowerCase().contains("mac")
                ? System.getProperty("user.home") + "/Library/.DreamCache"
                : System.getenv("LOCALAPPDATA") + "/.DreamCache";
    }

    public static class Config {
        private String nickName;
    }

    public DreamLauncher() {
        initializeWindow();
    }

    private void initializeWindow() {
        try {
            ImageIcon logo = new ImageIcon(Objects.requireNonNull(getClass().getResource("/Images/logo.png")));
            JLabel logoLabel = new JLabel(logo);
            Dimension size = logoLabel.getPreferredSize();
            logoLabel.setBounds(300, 203, size.width, size.height);
            add(logoLabel);
        } catch (Exception e) {
            System.err.println("Failed to load logo: " + e.getMessage());
        }
    }

    private void center() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

    private static Config loadConfig() {
        Path filePath = Path.of(folderPath, "autofill.json");
        try {
            String content = Files.readString(filePath);
            return new Gson().fromJson(content, Config.class);
        } catch (IOException e) {
            return new Config();
        }
    }

    private static void saveConfig(Config config) {
        Path filePath = Path.of(folderPath, "autofill.json");
        try {
            String json = new Gson().toJson(config);
            Files.writeString(filePath, json);
        } catch (IOException e) {
            System.err.println("Failed to save config file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            display = new DreamLauncher();
            ensureFolderExists();
            // config = loadConfig();
            //if (config.nickName != null) {
            //  System.out.println("Auto-fill successful, changing screen to MainScreen...");
            //  changeToMainScreen(config.nickName);
            // } else {
            // System.out.println("No auto-fill data, showing launcher screen...");
            // setupLauncherScreen();
            //}
        });
    }

    private static void ensureFolderExists() {
        File folder = new File(folderPath);
        if (!folder.exists() && !folder.mkdirs()) {
            System.err.println("Failed to create cache folder");
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
        display.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        display.getContentPane().setBackground(BACKGROUND_COLOR);
        display.setLayout(null);

        display.add(createLauncherLabel());
        nickname = createNicknameField();
        display.add(nickname);
        display.add(createRectangleLabel());
        enterLabel = createEnterLabel();
        display.add(enterLabel);

        display.center();
        display.setResizable(false);
        display.setVisible(true);
    }

    private static JLabel createLauncherLabel() {
        JLabel launcherText = new JLabel("Launcher");
        launcherText.setForeground(LAUNCHER_COLOR);

        try (InputStream is = DreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Bold.ttf")) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is)).deriveFont(56.0f);
            launcherText.setFont(font);
            launcherText.setBounds(478, -19, 2000, 600);
        } catch (IOException | FontFormatException e) {
            System.err.println("Failed to load font: " + e.getMessage());
            launcherText.setFont(new Font("Arial", Font.BOLD, 56));
        }

        return launcherText;
    }

    private static JTextField createNicknameField() {
        JTextField nicknameField = new JTextField(DEFAULT_NICKNAME_TEXT);
        nicknameField.setBorder(null);
        nicknameField.setOpaque(false);
        nicknameField.setForeground(NICKNAME_COLOR);
        nicknameField.setBounds(315, 375, 385, 60);

        nicknameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isTextChanged.get()) {
                    nicknameField.setText("");
                    isTextChanged.set(true);
                    nicknameField.setForeground(NICKNAME_COLOR);
                }
            }
        });

        try (InputStream fontStream = DreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Medium.ttf")) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(fontStream)).deriveFont(16.0f);
            nicknameField.setFont(font);
        } catch (IOException | FontFormatException e) {
            System.err.println("Failed to load font: " + e.getMessage());
            nicknameField.setFont(new Font("Arial", Font.PLAIN, 16));
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
        try {
            ImageIcon rectangle = new ImageIcon(Objects.requireNonNull(DreamLauncher.class.getResource("/Images/rectangle.png")));
            JLabel rectangleLabel = new JLabel(rectangle);
            Dimension size = rectangleLabel.getPreferredSize();
            rectangleLabel.setBounds(293, 380, size.width, size.height);
            return rectangleLabel;
        } catch (Exception e) {
            System.err.println("Failed to load rectangle image: " + e.getMessage());
            return new JLabel();
        }
    }

    private static JLabel createEnterLabel() {
        try {
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
        } catch (Exception e) {
            System.err.println("Failed to load enter image: " + e.getMessage());
            return new JLabel();
        }
    }

    private static void performAction() {
        String enteredNickname = nickname.getText().trim();

        if (isValidNickname(enteredNickname)) {
            changeToMainScreen(enteredNickname);
            config.nickName = enteredNickname;
            saveConfig(config);
        } else {
            nickname.setText(ERROR_NICKNAME_TEXT);
            nickname.setForeground(ERROR_COLOR);
        }
    }

    private static boolean isValidNickname(String nickname) {
        return nickname != null && !nickname.isEmpty() && nickname.matches("[a-zA-Z0-9_-]+");
    }

    private static ImageIcon getBrighterIcon(ImageIcon icon) {
        Image img = icon.getImage();
        BufferedImage bufferedImage = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(img, 0, 0, null);
        graphics.dispose();

        float brightnessIncrease = 1.4f;
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int rgb = bufferedImage.getRGB(x, y);
                Color color = new Color(rgb, true);
                int r = Math.min((int) (color.getRed() * brightnessIncrease), 255);
                int g = Math.min((int) (color.getGreen() * brightnessIncrease), 255);
                int b = Math.min((int) (color.getBlue() * brightnessIncrease), 255);
                bufferedImage.setRGB(x, y, new Color(r, g, b, color.getAlpha()).getRGB());
            }
        }

        return new ImageIcon(bufferedImage);
    }
}