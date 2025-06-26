package xyz.dream.launcher;

import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import xyz.dream.launcher.managers.Downloader;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MainScreen extends JFrame {
    static String currentVersion;
    private static boolean makeOptimization;
    private static boolean makeUpdateCheck;
    private static boolean makeWhiteTheme;
    private static boolean makeFreecam;
    private static final boolean isOptimizationOn;
    private static final boolean isWhiteTheme;
    private static final boolean isFreecamOn;
    private static final boolean isUpdatesOn;
    private static String valueFromFile;
    private static String downloadDirectory;
    private static Color defaultTextColor;
    private static Color defaultTextColor5;
    private static Color defaultTextColorWhite;
    private static Color defaultTextColorSettings;
    static int xmx;

    // Main screen initializer
    MainScreen() {

        // Check folder
        checkFolder();

        // Check for updates
        this.checkGithubRelease();

        // Set window properties
        setTitle("Frogdream Launcher");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1022, 600);
        getContentPane().setBackground(new Color(12, 12, 12));
        setLayout(null);
        setResizable(false);

        // Make screen on center
        centerWindow();
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int frameWidth = getWidth();
        int frameHeight = getHeight();
        int x = (screenWidth - frameWidth) / 2;
        int y = (screenHeight - frameHeight) / 2;
        setLocation(x, y);
    }

    // Version
    private void checkGithubRelease() {

        String currentVersion = "v1.3.0.1";

        MainScreen.GithubReleaseChecker releaseChecker = new MainScreen.GithubReleaseChecker();
        releaseChecker.checkGithubRelease("Frogdream", "Launcher", currentVersion);
    }

    // Check folder
    public static void checkFolder() {
        String targetFolder = getGamePath();

        File folder = new File(targetFolder);

        if (folder.exists() && folder.isDirectory()) {
            System.out.println("Game folder exists.");
        } else {
            System.out.println("Game folder doesn't exist. Creating will be started.");
        }
    }

    // Open download folder
    private static void openDownloadsFolder() {
        String osName = System.getProperty("os.name").toLowerCase();
        String downloadDirectory;
        if (osName.contains("win")) {
            downloadDirectory = System.getenv("LOCALAPPDATA") + "\\.FrogDream";
        } else if (osName.contains("mac")) {
            downloadDirectory = System.getProperty("user.home") + "/.FrogDream";
        } else if (osName.contains("linux")) {
            downloadDirectory = System.getProperty("user.home") + "/.FrogDream";
        } else {
            JOptionPane.showMessageDialog(null, "Your system is not supported. Please, contact with developers.");
            return;
        }

        File file = new File(downloadDirectory);

        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "Game folder doesn't exist. It's because you didn't start the game yet.");
            System.out.println("Folder: " + downloadDirectory + " doesn't exist.");
            return;
        }

        try {
            Desktop.getDesktop().open(file);
        } catch (IOException var1) {
            throw new RuntimeException(var1);
        }
    }

    private static void checkSettings() {
        makeOptimization = isOptimizationOn;
        makeUpdateCheck = isUpdatesOn;
        makeWhiteTheme = isWhiteTheme;
        makeFreecam = isFreecamOn;
    }

    private static String getAppDataPath() {
        String appDataPath;

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            appDataPath = System.getenv("LOCALAPPDATA") + "\\FrogDreamCache\\";
        } else if (os.contains("mac")) {
            appDataPath = System.getProperty("user.home") + "/Library/FrogDreamCache/";
        } else {
            appDataPath = System.getProperty("user.home") + "/.FrogDreamCache/";
        }

        return appDataPath;
    }

    private static String getGamePath() {
        String gamePath;

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            gamePath = System.getenv("LOCALAPPDATA") + "\\.FrogDream";
        } else if (os.contains("mac")) {
            gamePath = System.getProperty("user.home") + "/.FrogDream";
        } else {
            gamePath = System.getProperty("user.home") + "/.FrogDream";
        }

        return gamePath;
    }

    static {
        String appDataPath = getAppDataPath();

        isOptimizationOn = readBooleanValueFromFile(appDataPath + "optimization.txt");
        isUpdatesOn = readBooleanValueFromFile(appDataPath + "updates.txt");
        isWhiteTheme = readBooleanValueFromFile(appDataPath + "whitetheme.txt");
        isFreecamOn = readBooleanValueFromFile(appDataPath + "freecam.txt");
    }

    // GitHub release checker
    public static class GithubReleaseChecker {
        public void checkGithubRelease(String repoOwner, String repoName, String currentVersion) {
            String url = String.format("https://api.github.com/repos/%s/%s/releases", repoOwner, repoName);
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    JSONArray releases = new JSONArray(response.toString());

                    checkSettings();

                    for (int i = 0; i < releases.length(); ++i) {
                        JSONObject release = releases.getJSONObject(i);
                        String tagName = release.getString("tag_name"); // new version a
                        if (this.isNewerVersion(tagName, currentVersion)) {
                            System.out.println("New version available: " + tagName + ".");
                            if (makeUpdateCheck) {
                                int option = JOptionPane.showOptionDialog(null,
                                        "New version is available!", "Launcher update",
                                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                                        new String[]{"Later", "Update"}, "Update");
                                if (option == 1) {
                                    Desktop.getDesktop().browse(new URI("https://github.com/Frogdream/Launcher/releases"));
                                }
                                return;
                            }
                        }
                    }
                    System.out.println("Launcher is up to date.");
                } else {
                    System.out.println("Failed to check for updates.");
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        // Ignore -alpha tags and "v"
        private boolean isNewerVersion(String newVersion, String currentVersion) {
            newVersion = newVersion.replaceAll("^(v|-alpha)", "");
            currentVersion = currentVersion.replaceAll("^(v|-alpha)", "");
            String[] newVersionParts = newVersion.split("\\.");
            String[] currentVersionParts = currentVersion.split("\\.");
            int minLength = Math.min(newVersionParts.length, currentVersionParts.length);

            // Old/New version check
            for (int i = 0; i < minLength; ++i) {
                int newPart = this.parseVersionPart(newVersionParts[i]);
                int currentPart = this.parseVersionPart(currentVersionParts[i]);
                if (newPart > currentPart) {
                    return true;
                }

                if (newPart < currentPart) {
                    return false;
                }
            }

            return newVersionParts.length > currentVersionParts.length;
        }

        private int parseVersionPart(String versionPart) {
            if (versionPart.contains("-alpha")) {
                versionPart = versionPart.replace("-alpha", "");
            }

            return Integer.parseInt(versionPart);
        }
    }

    // Main
    public static class MainScreenInitializer {
        public static void initialize(final MainScreen mainScreen, final String enteredNickname) {

            // Console and logs

            mainScreen.addKeyListener(new KeyAdapter() {
                                          @Override
                                          public void keyPressed(KeyEvent e) {
                                              if (e.getKeyCode() == KeyEvent.VK_C) {

                                                  JFrame console = new JFrame("Frogdream Launcher Console");
                                                  console.setSize(500, 500);
                                                  console.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                                                  console.setVisible(true);
                                                  console.setLayout(null);
                                                  console.setResizable(false);

                                                  JTextArea consoleText = new JTextArea();
                                                  consoleText.setBounds(0, 0, 500, 500);
                                                  consoleText.setEditable(false);
                                                  consoleText.setBackground(new Color(12, 12, 12));
                                                  consoleText.setForeground(new Color(255, 255, 255));
                                                  consoleText.setFont(new Font("Consolas", Font.PLAIN, 14));
                                                  console.add(consoleText);

                                                  JScrollPane scroll = new JScrollPane(consoleText);
                                                  scroll.setBounds(0, 0, 500, 500);
                                                  console.add(scroll);

                                                  consoleText.setText("FrogDream Launcher Console");

                                                      ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                                                      executorService.scheduleAtFixedRate(() -> consoleText.setText("Console Launcher Stats\n" +
                                                              "Version: " + "1.2.0" + "\n" +

                                                              "Optimization: " + makeOptimization + "\n" +
                                                              "Updates: " + makeUpdateCheck + "\n" +
                                                              "White theme: " + makeWhiteTheme + "\n" +
                                                              "Freecam: " + makeFreecam + "\n" +

                                                              "Nickname: " + enteredNickname + "\n" +
                                                              "Xmx: " + valueFromFile + "\n" +
                                                              "Directory: " + downloadDirectory + "\n" +

                                                              "OS: " + System.getProperty("os.name") + "\n" +
                                                              "OS version: " + System.getProperty("os.version") + "\n" +
                                                              "OS architecture: " + System.getProperty("os.arch") + "\n" +

                                                              "Java version: " + System.getProperty("java.version") + "\n" +
                                                              "Java vendor: " + System.getProperty("java.vendor") + "\n" +

                                                              "Available processors: " + Runtime.getRuntime().availableProcessors() + "\n" +
                                                              "Free RAM: " + Runtime.getRuntime().freeMemory() / 1024L / 1024L + "\n" +
                                                              "Max RAM: " + Runtime.getRuntime().maxMemory() / 1024L / 1024L + "\n"), 0, 10000, TimeUnit.MILLISECONDS);

                        JFrame console2 = new JFrame("Logs");
                        console2.setSize(750, 500);
                        console2.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                        console2.setVisible(true);
                        console2.setLayout(null);
                        console2.setResizable(false);

                        JTextArea consoleText2 = new JTextArea();
                        consoleText2.setBounds(0, 0, 750, 500);
                        consoleText2.setEditable(false);
                        consoleText2.setBackground(new Color(67, 109, 48, 255));
                        consoleText2.setForeground(new Color(255, 255, 255));
                        consoleText2.setFont(new Font("Consolas", Font.PLAIN, 14));
                        console2.add(consoleText2);

                        JScrollPane scroll2 = new JScrollPane(consoleText2);
                        scroll2.setBounds(0, 0, 750, 500);
                        console2.add(scroll2);

                        System.setOut(new PrintStream(new OutputStream() {
                            @Override
                            public void write(int b) {
                                consoleText2.append(String.valueOf((char) b));
                            }
                        }));
                    }
                }
            });

            // Appdata path
            String appDataPath = getAppDataPath();
                String filePath = System.getenv("LOCALAPPDATA") + "/FrogDreamCache/xmx.txt";

                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    filePath = System.getProperty("user.home") + "/Library/FrogDreamCache/xmx.txt";
                }

                try {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            valueFromFile = readValueFromFile(appDataPath + "xmx.txt");

            // Fonts
            InputStream is = DreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Bold.ttf");
            InputStream is2 = DreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Medium.ttf");
            Color defaultTextColor = new Color(99, 99, 99);

            Font font2;
            try {
                assert is2 != null;

                font2 = Font.createFont(0, is2);
            } catch (IOException | FontFormatException var4) {
                throw new RuntimeException(var4);
            }

            InputStream is4 = DreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Medium.ttf");
            Color defaultTextColor5 = new Color(99, 99, 99);

            Font font4;
            try {
                assert is4 != null;

                font4 = Font.createFont(0, is4);
            } catch (IOException | FontFormatException var4) {
                throw new RuntimeException(var4);
            }

            Font font1;
            try {
                assert is != null;

                font1 = Font.createFont(0, is);
            } catch (IOException | FontFormatException var17) {
                throw new RuntimeException(var17);
            }

            InputStream thisWhite = DreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Medium.ttf");
            Color defaultTextColorWhite = new Color(255, 255, 255);

            Font font3;
            try {
                assert thisWhite != null;

                font3 = Font.createFont(0, thisWhite);
            } catch (IOException | FontFormatException var4) {
                throw new RuntimeException(var4);
            }

            InputStream thisSettings = DreamLauncher.class.getResourceAsStream("/Fonts/GolosText-SemiBold.ttf");
            Color defaultTextColorSettings = new Color(81, 81, 81);

            Font font5;
            try {
                assert thisSettings != null;

                font5 = Font.createFont(0, thisSettings);
            } catch (IOException | FontFormatException var4) {
                throw new RuntimeException(var4);
            }

            if (makeWhiteTheme) {
                mainScreen.setContentPane(new JLabel(new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whitebg.png")))));
            } else {
                mainScreen.setContentPane(new JLabel(new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/bg.png")))));
            }

            // Will be true in 1.3.0 update
            mainScreen.setUndecorated(false);

            // Launcher text
            JLabel launcherText = new JLabel("Launcher");
            launcherText.setForeground(new Color(116, 168, 50));

            // Settings text
            JLabel settingsTextLabel = new JLabel("Settings");
            settingsTextLabel.setForeground(defaultTextColorSettings);
            Font sizedSettingsTextLabel = font5.deriveFont(24.0F);
            settingsTextLabel.setFont(sizedSettingsTextLabel);
            settingsTextLabel.setBounds(743+21, 39, 1000, 36);

            // Don't add it and other settings labels to mainScreen!

            // Play text
            JLabel playTextLabel = new JLabel("Play");
            playTextLabel.setForeground(defaultTextColorWhite);
            Font sizedFontForPlayText = font3.deriveFont(16.0F);
            playTextLabel.setFont(sizedFontForPlayText);
            playTextLabel.setBounds(377, 188, 100, 100);
            mainScreen.add(playTextLabel);

            // Play text 2
            JLabel playTextLabel2 = new JLabel("Swift-play");
            playTextLabel2.setForeground(defaultTextColorWhite);
            Font sizedFontForPlayText2 = font3.deriveFont(16.0F);
            playTextLabel2.setFont(sizedFontForPlayText2);
            playTextLabel2.setBounds(125, 188, 100, 100);
            //mainScreen.add(playTextLabel2);

            ImageIcon logo = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/smallLogo.png")));
            JLabel logoLabel = new JLabel(logo);
            logoLabel.setBounds(224, 20, 75, 75);

            ImageIcon whiteLogo = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteLogo.png")));
            JLabel whiteLogoLabel = new JLabel(whiteLogo);
            whiteLogoLabel.setBounds(224, 20, 75, 75);

            if (makeWhiteTheme) {
                mainScreen.add(whiteLogoLabel);
            } else {
                mainScreen.add(logoLabel);
            }

            // Wiki
            final ImageIcon wiki = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/wiki.png")));
            final JLabel wikiLabel = new JLabel(wiki);
            wikiLabel.setBounds(24, 89, 24, 24);

            mainScreen.add(wikiLabel);

            // Go2/3 for pictures
            ImageIcon go2 = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/go.png")));
            JLabel go2Label = new JLabel(go2);
            go2Label.setBounds(987, 243, 10, 18);
            mainScreen.add(go2Label);

            ImageIcon go3 = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/go.png")));
            JLabel go3Label = new JLabel(go3);
            go3Label.setBounds(987, 508, 10, 18);
            mainScreen.add(go3Label);

            // Map
            JLabel mapTextLabel = new JLabel("Server map");
            mapTextLabel.setForeground(defaultTextColorWhite);
            Font sizedMapTextLabel = font3.deriveFont(16.0F);
            mapTextLabel.setFont(sizedMapTextLabel);
            mapTextLabel.setBounds(887, 508, 1000, 16);
            mainScreen.add(mapTextLabel);

            // Settings
            JLabel clickParasha = new JLabel();
            clickParasha.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            clickParasha.setBounds(854, 217, 133, 133);
            mainScreen.add(clickParasha);

            /*ImageIcon version = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/version.png")));
            JLabel versionLabel = new JLabel(version);
            versionLabel.setBounds(107, 213, 198, 50);*/

            // Version text
            /*JLabel versionTextLabel = new JLabel("Choose version");
            versionTextLabel.setForeground(defaultTextColorWhite);
            Font sizedVersionTextLabel = font3.deriveFont(16.0F);
            versionTextLabel.setFont(sizedVersionTextLabel);
            versionTextLabel.setBounds(167, 188, 200, 100);*/

            // Left
            /*ImageIcon left = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/left.png")));
            JLabel leftLabel = new JLabel(left);
            leftLabel.setBounds(129, 229, 10, 18);*/

            //mainScreen.add(leftLabel);
            //mainScreen.add(versionTextLabel);
            //mainScreen.add(versionLabel);

            ImageIcon green = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/green_l.png")));
            JLabel greenLabel = new JLabel(green);
            greenLabel.setBounds(0, 552, 1022,20);

            // Text on green
            JLabel greenText = new JLabel("In the new 1.5 version you will see a new launcher design");
            greenText.setForeground(new Color(255, 255, 255));
            Font sizedGreenText = font3.deriveFont(14.0F);
            greenText.setFont(sizedGreenText);
            greenText.setBounds(320, 552, 393, 17);

            mainScreen.add(greenText);
            mainScreen.add(greenLabel);

            // White / black buttons
            final String onButton;
            final String offButton;

            if (makeWhiteTheme) {
                onButton = "whiteOn";
                offButton = "whiteOff";
            } else {
                onButton = "on";
                offButton = "off";
            }


            // Optimization
            final boolean[] isOptimizationOn = {readBooleanValueFromFile(appDataPath + "optimization.txt")};
            final ImageIcon[] optimizationIcon = {isOptimizationOn[0] ? new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + onButton + ".png"))) : new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + offButton + ".png")))};

            JLabel onLabel = new JLabel(optimizationIcon[0]);
            onLabel.setBounds(852, 214, 134, 50);

            onLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            onLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    isOptimizationOn[0] = !isOptimizationOn[0];
                    optimizationIcon[0] = isOptimizationOn[0] ? new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + onButton + ".png"))) :
                            new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + offButton + ".png")));

                    onLabel.setIcon(optimizationIcon[0]);
                    saveBooleanValueToFile("optimization.txt", isOptimizationOn[0]);

                }

            });

            // White on optimization label
            JLabel whiteOnLabel = new JLabel(optimizationIcon[0]);
            whiteOnLabel.setBounds(852, 214, 134, 50);

            whiteOnLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            whiteOnLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    isOptimizationOn[0] = !isOptimizationOn[0];
                    optimizationIcon[0] = isOptimizationOn[0] ? new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteOn.png"))) :
                            new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteOff.png")));

                    whiteOnLabel.setIcon(optimizationIcon[0]);
                    saveBooleanValueToFile("optimization.txt", isOptimizationOn[0]);

                }

            });

            // Updates
            final boolean[] areUpdatesOn = {readBooleanValueFromFile(appDataPath + "updates.txt")};
            final ImageIcon[] updatesIcon = {areUpdatesOn[0] ? new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + onButton + ".png"))) :
                    new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + offButton + ".png")))};
            JLabel onUpdatesLabel = new JLabel(updatesIcon[0]);
            onUpdatesLabel.setBounds(852, 289, 134, 50);

            onUpdatesLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            onUpdatesLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    areUpdatesOn[0] = !areUpdatesOn[0];
                    updatesIcon[0] = areUpdatesOn[0] ? new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + onButton + ".png"))) :
                            new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + offButton + ".png")));

                    onUpdatesLabel.setIcon(updatesIcon[0]);
                    saveBooleanValueToFile("updates.txt", areUpdatesOn[0]);
                }
            });

            // White on updates label
            JLabel whiteOnUpdatesLabel = new JLabel(updatesIcon[0]);
            whiteOnUpdatesLabel.setBounds(852, 289, 134, 50);

            whiteOnUpdatesLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            whiteOnUpdatesLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    areUpdatesOn[0] = !areUpdatesOn[0];
                    updatesIcon[0] = areUpdatesOn[0] ? new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteOn.png"))) :
                            new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteOff.png")));

                    whiteOnUpdatesLabel.setIcon(updatesIcon[0]);
                    saveBooleanValueToFile("updates.txt", areUpdatesOn[0]);
                }
            });

            // White theme
            final boolean[] isWhiteTheme = {readBooleanValueFromFile(appDataPath + "whitetheme.txt")};
            final ImageIcon[] whiteThemeIcon = {isWhiteTheme[0] ? new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + onButton + ".png"))) : new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + offButton + ".png")))};
            JLabel offLabel = new JLabel(whiteThemeIcon[0]);
            offLabel.setBounds(852, 367, 134, 50);

            offLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    isWhiteTheme[0] = !isWhiteTheme[0];
                    whiteThemeIcon[0] = isWhiteTheme[0] ? new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + onButton + ".png"))) :
                            new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/" + offButton + ".png")));
                    offLabel.setIcon(whiteThemeIcon[0]);
                    saveBooleanValueToFile("whitetheme.txt", isWhiteTheme[0]);

                    UIManager.put("OptionPane.okButtonText", "Okay");
                    JOptionPane.showMessageDialog(null, "You need to restart launcher for new theme.", "Restart launcher", JOptionPane.INFORMATION_MESSAGE);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    offLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    offLabel.setCursor(Cursor.getDefaultCursor());
                }
            });

            // White theme white off label
            JLabel whiteOffLabel = new JLabel(whiteThemeIcon[0]);
            whiteOffLabel.setBounds(852, 367, 134, 50);

            whiteOffLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    isWhiteTheme[0] = !isWhiteTheme[0];
                    whiteThemeIcon[0] = isWhiteTheme[0] ? new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteOn.png"))) :
                            new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteOff.png")));
                    whiteOffLabel.setIcon(whiteThemeIcon[0]);
                    saveBooleanValueToFile("whitetheme.txt", isWhiteTheme[0]);

                    UIManager.put("OptionPane.okButtonText", "Okay");
                    JOptionPane.showMessageDialog(null, "You need to restart launcher for new theme.", "Restart launcher", JOptionPane.INFORMATION_MESSAGE);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    whiteOffLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    whiteOffLabel.setCursor(Cursor.getDefaultCursor());
                }
            });

            // RAM
            JLabel smallrectangleTextLabel = new JLabel("RAM allocation");
            smallrectangleTextLabel.setForeground(defaultTextColor5);
            Font sizedSmallrectangleTextLabel = font4.deriveFont(16.0F);
            smallrectangleTextLabel.setFont(sizedSmallrectangleTextLabel);
            smallrectangleTextLabel.setBounds(653, 149, 1000, 16);

            JTextField smallrectangleTextLabel2 = new JTextField(valueFromFile);

            smallrectangleTextLabel2.setForeground(defaultTextColor5);
            Font sizedSmallrectangleTextLabel2 = font4.deriveFont(16.0F);
            smallrectangleTextLabel2.setFont(sizedSmallrectangleTextLabel2);

            smallrectangleTextLabel2.setOpaque(false);
            smallrectangleTextLabel2.setBorder(null);
            smallrectangleTextLabel2.setBounds(882, 149, 1000, 16);

            ((AbstractDocument) smallrectangleTextLabel2.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                        throws BadLocationException {
                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    currentText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                    if (isValidInput(currentText)) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }

                private boolean isValidInput(String text) {
                    try {
                        int value = Integer.parseInt(text);
                        return value >= 1 && value <= 64;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            });

            smallrectangleTextLabel2.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    String text = smallrectangleTextLabel2.getText();

                    String localAppDataPath = System.getenv("LOCALAPPDATA");

                    if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                        localAppDataPath = System.getProperty("user.home") + "/Library";
                    }
                    if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                        localAppDataPath = System.getProperty("user.home") + ".local/";
                    }

                    if (localAppDataPath != null) {
                        String filePath = System.getenv("LOCALAPPDATA") + "/FrogDreamCache/xmx.txt";

                        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                            filePath = System.getProperty("user.home") + "/Library/FrogDreamCache/xmx.txt";
                        }
                        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                            filePath = System.getProperty("user.home") + "/.config/FrogDreamCache/xmx.txt";
                        }

                        try {
                            FileWriter fileWriter = new FileWriter(filePath, false);
                            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                            bufferedWriter.write(text);
                            bufferedWriter.close();

                            fileWriter.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        readValueFromFile(localAppDataPath + "xmx.txt");
                    }

                    System.out.println("New RAM value: " + text);
                }
            });

            // Default value
            if (smallrectangleTextLabel2.getText().isEmpty()) {
                xmx = 6;
                smallrectangleTextLabel2.setText("6");
            } else {
                xmx = Integer.parseInt(smallrectangleTextLabel2.getText());
            }

            // Settings rectangle
            ImageIcon settingsrectangle = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/settingsRectangle.png")));
            JLabel settingsrectangleLabel = new JLabel(settingsrectangle);
            Dimension settingsrectangleSize = settingsrectangleLabel.getPreferredSize();
            settingsrectangleLabel.setBounds(634, 213, settingsrectangleSize.width, settingsrectangleSize.height);

            ImageIcon whiteSettingsrectangle = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteSettingsRectangle.png")));
            JLabel whiteSettingsrectangleLabel = new JLabel(whiteSettingsrectangle);
            Dimension whiteSettingsrectangleSize = whiteSettingsrectangleLabel.getPreferredSize();
            whiteSettingsrectangleLabel.setBounds(634, 213, whiteSettingsrectangleSize.width, whiteSettingsrectangleSize.height);

            if (makeOptimization) {
                mainScreen.add(whiteSettingsrectangleLabel);
            } else {
                mainScreen.add(settingsrectangleLabel);
            }

            // Client optimization
            JLabel smallrectangleTextLabel4 = new JLabel("Client optimization");
            smallrectangleTextLabel4.setForeground(defaultTextColor5);
            Font sizedSmallrectangleTextLabel4 = font4.deriveFont(16.0F);
            smallrectangleTextLabel4.setFont(sizedSmallrectangleTextLabel4);
            smallrectangleTextLabel4.setBounds(653, 230, 1000, 16);
            mainScreen.add(smallrectangleTextLabel4);
            mainScreen.remove(smallrectangleTextLabel4);

            // Updates
            JLabel smallrectangleTextLabel5 = new JLabel("Updates");
            smallrectangleTextLabel5.setForeground(defaultTextColor5);
            Font sizedSmallrectangleTextLabel5 = font4.deriveFont(16.0F);
            smallrectangleTextLabel5.setFont(sizedSmallrectangleTextLabel5);
            smallrectangleTextLabel5.setBounds(653, 307, 1000, 16);
            mainScreen.add(smallrectangleTextLabel5);
            mainScreen.remove(smallrectangleTextLabel5);

            // Dark theme
            JLabel smallrectangleTextLabel6 = new JLabel("White theme");
            smallrectangleTextLabel6.setForeground(defaultTextColor5);
            Font sizedSmallrectangleTextLabel6 = font4.deriveFont(16.0F);
            smallrectangleTextLabel6.setFont(sizedSmallrectangleTextLabel6);
            smallrectangleTextLabel6.setBounds(653, 307 + 77, 1000, 16);
            mainScreen.add(smallrectangleTextLabel6);
            mainScreen.remove(smallrectangleTextLabel6);

            // GB text
            JLabel smallrectangleTextLabel3 = new JLabel("Gb");
            smallrectangleTextLabel3.setForeground(defaultTextColor5);
            Font sizedSmallrectangleTextLabel3 = font4.deriveFont(16.0F);
            smallrectangleTextLabel3.setFont(sizedSmallrectangleTextLabel3);
            smallrectangleTextLabel3.setBounds(943, 149, 1000, 16);
            mainScreen.add(smallrectangleTextLabel3);
            mainScreen.remove(smallrectangleTextLabel3);

            ImageIcon smallrectangle = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/smallRectangle.png")));
            JLabel smallrectangleLabel = new JLabel(smallrectangle);
            Dimension smallrectangleSize = smallrectangleLabel.getPreferredSize();
            smallrectangleLabel.setBounds(634, 132, smallrectangleSize.width, smallrectangleSize.height);
            mainScreen.add(smallrectangleLabel);
            mainScreen.remove(smallrectangleLabel);

            ImageIcon whiteSmallrectangle = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteSmallRectangle.png")));
            JLabel whiteSmallrectangleLabel = new JLabel(whiteSmallrectangle);
            Dimension whiteSmallrectangleSize = whiteSmallrectangleLabel.getPreferredSize();
            whiteSmallrectangleLabel.setBounds(634, 132, whiteSmallrectangleSize.width, whiteSmallrectangleSize.height);
            mainScreen.add(whiteSmallrectangleLabel);
            mainScreen.remove(whiteSmallrectangleLabel);

            // Settings end

            SwingUtilities.invokeLater(() -> {

                        // News text
                        JLabel newsTextLabel = new JLabel("News");
                        newsTextLabel.setForeground(defaultTextColorWhite);
                        Font sizedNewsTextLabel = font3.deriveFont(16.0F);
                        newsTextLabel.setFont(sizedNewsTextLabel);
                        newsTextLabel.setBounds(930, 243, 1000, 16);
                        mainScreen.add(newsTextLabel);

                        // News
                        final ImageIcon news = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/news.png")));
                        final JLabel newsLabel = new JLabel(news);
                        newsLabel.setBounds(642, 24, 449, 250);
                        newsLabel.addMouseListener(new MouseAdapter() {
                            public void mouseEntered(MouseEvent e) {
                                mainScreen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                newsLabel.setIcon(MainScreen.getBrighterIcon(news, 1.2F));
                            }

                            public void mouseExited(MouseEvent e) {
                                mainScreen.setCursor(Cursor.getDefaultCursor());
                                newsLabel.setIcon(news);
                            }

                            public void mouseClicked(MouseEvent e) {
                                try {
                                    Desktop.getDesktop().browse(new URI("https://vk.com/frogdream"));
                                } catch (URISyntaxException | IOException e1) {
                                    e1.printStackTrace();
                                }

                            }
                        });

                        // Wiki
                        wikiLabel.addMouseListener(new MouseAdapter() {
                            public void mouseEntered(MouseEvent e) {
                                wikiLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                wikiLabel.setIcon(MainScreen.getBrighterIcon(wiki, 1.4F));
                            }

                            public void mouseExited(MouseEvent e) {
                                wikiLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                wikiLabel.setIcon(wiki);
                            }

                            public void mouseClicked(MouseEvent e) {
                                try {
                                    Desktop.getDesktop().browse(new URI("https://wiki.frogdream.xyz"));
                                } catch (URISyntaxException | IOException e2) {
                                    e2.printStackTrace();
                                }

                            }
                        });

                        // Update launcher
                        final ImageIcon updateLauncher = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/updateLauncher.png")));
                        final JLabel updateLauncherLabel = new JLabel(updateLauncher);
                        updateLauncherLabel.setBounds(24, 141, 24, 24);
                        mainScreen.add(updateLauncherLabel);
                        updateLauncherLabel.addMouseListener(new MouseAdapter() {
                            public void mouseEntered(MouseEvent e) {
                                updateLauncherLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                updateLauncherLabel.setIcon(MainScreen.getBrighterIcon(updateLauncher, 1.4F));
                            }

                            public void mouseExited(MouseEvent e) {
                                updateLauncherLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                updateLauncherLabel.setIcon(updateLauncher);
                            }

                            public void mouseClicked(MouseEvent e) {
                                try {
                                    Desktop.getDesktop().browse(new URI("https://www.frogdream.xyz/launcher"));
                                } catch (URISyntaxException | IOException e3) {
                                    e3.printStackTrace();
                                }

                            }
                        });
                        final ImageIcon premium = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/cart.png")));
                        final JLabel premiumLabel = new JLabel(premium);
                        premiumLabel.setBounds(24, 193, 24, 24);
                        mainScreen.add(premiumLabel);

                        // Premium
                        premiumLabel.addMouseListener(new MouseAdapter() {
                            public void mouseEntered(MouseEvent e) {
                                premiumLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                premiumLabel.setIcon(MainScreen.getBrighterIcon(premium, 1.4F));
                            }

                            public void mouseExited(MouseEvent e) {
                                premiumLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                premiumLabel.setIcon(premium);
                            }

                            public void mouseClicked(MouseEvent e) {
                                try {
                                    Desktop.getDesktop().browse(new URI("https://buy.frogdream.xyz"));
                                } catch (URISyntaxException | IOException var3) {
                                    var3.printStackTrace();
                                }

                            }
                        });
                        final ImageIcon friend = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/bg.png")));
                        final JLabel friendLabel = new JLabel(friend);
                        friendLabel.setBounds(25, 245, 24, 24);
                        //mainScreen.add(friendLabel);

                        // Friend
                        friendLabel.addMouseListener(new MouseAdapter() {
                            public void mouseEntered(MouseEvent e) {
                                friendLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                friendLabel.setIcon(MainScreen.getBrighterIcon(friend, 1.4F));
                            }

                            public void mouseExited(MouseEvent e) {
                                friendLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                friendLabel.setIcon(friend);
                            }

                            public void mouseClicked(MouseEvent e) {
                                try {
                                    Desktop.getDesktop().browse(new URI("https://frogdream.xyz/friend"));
                                } catch (URISyntaxException | IOException var3) {
                                    var3.printStackTrace();
                                }

                            }
                        });

                        class ActiveContainer {
                            boolean active;
                        }

                        final ActiveContainer activeContainer = new ActiveContainer();
                        activeContainer.active = true;

                        final ImageIcon settings = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/settings.png")));
                        final JLabel settingsLabel = new JLabel(settings);
                        settingsLabel.setBounds(25, 248, 24, 24);
                        mainScreen.add(settingsLabel);

                        // Settings implementation 2nd step (visibility for main labels)
                        if (makeWhiteTheme) {
                            mainScreen.add(whiteSettingsrectangleLabel);
                            whiteSettingsrectangleLabel.setVisible(false);
                        } else {
                            mainScreen.add(smallrectangleLabel);
                            mainScreen.add(settingsrectangleLabel);
                            smallrectangleLabel.setVisible(false);
                            settingsrectangleLabel.setVisible(false);
                        }

                        // Map
                        ImageIcon map = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/map.png")));
                        JLabel mapLabel = new JLabel(map);
                        mapLabel.setBounds(639, 291, 449, 250);
                        mapLabel.addMouseListener(new MouseAdapter() {
                            public void mouseEntered(MouseEvent e) {
                                mainScreen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                mapLabel.setIcon(MainScreen.getBrighterIcon(map, 1.2F));
                            }

                            public void mouseExited(MouseEvent e) {
                                mainScreen.setCursor(Cursor.getDefaultCursor());
                                mapLabel.setIcon(map);
                            }

                            public void mouseClicked(MouseEvent e) {
                                try {
                                    Desktop.getDesktop().browse(new URI("https://map.frogdream.xyz"));
                                } catch (URISyntaxException | IOException var3) {
                                    var3.printStackTrace();
                                }

                            }
                        });

                        // Versions text
                        JLabel versionsTextLabel = new JLabel("Versions");
                        versionsTextLabel.setForeground(defaultTextColorSettings);
                        Font sizedVersionsTextLabel = font5.deriveFont(24.0F);
                        versionsTextLabel.setFont(sizedVersionsTextLabel);
                        versionsTextLabel.setBounds(762, 39, 1000, 36);
                        mainScreen.add(versionsTextLabel);
                        versionsTextLabel.setVisible(false);

                        // No versions
                        JLabel nosmallrectangleTextLabel = new JLabel("1.20.2");
                        nosmallrectangleTextLabel.setForeground(defaultTextColor5);
                        Font sizednoSmallrectangleTextLabel = font4.deriveFont(16.0F);
                        nosmallrectangleTextLabel.setFont(sizednoSmallrectangleTextLabel);
                        nosmallrectangleTextLabel.setBounds(653, 149, 1000, 16);
                        mainScreen.add(nosmallrectangleTextLabel);
                        nosmallrectangleTextLabel.setVisible(false);

                        // No versions
                        JLabel notwosmallrectangleTextLabel = new JLabel("No one versions found in the server");
                        notwosmallrectangleTextLabel.setForeground(defaultTextColor5);
                        Font sizednotwoSmallrectangleTextLabel = font4.deriveFont(16.0F);
                        notwosmallrectangleTextLabel.setFont(sizednotwoSmallrectangleTextLabel);
                        notwosmallrectangleTextLabel.setBounds(653, 149 + 81, 1000, 16);
                        mainScreen.add(notwosmallrectangleTextLabel);
                        notwosmallrectangleTextLabel.setVisible(false);

                        // No versions
                        JLabel nothreesmallrectangleTextLabel = new JLabel("No one versions found in the server");
                        nothreesmallrectangleTextLabel.setForeground(defaultTextColor5);
                        Font sizednothreeSmallrectangleTextLabel = font4.deriveFont(16.0F);
                        nothreesmallrectangleTextLabel.setFont(sizednothreeSmallrectangleTextLabel);
                        nothreesmallrectangleTextLabel.setBounds(653, 149 + 81 * 2, 1000, 16);
                        mainScreen.add(nothreesmallrectangleTextLabel);
                        nothreesmallrectangleTextLabel.setVisible(false);

                        // No versions
                        JLabel nofoursmallrectangleTextLabel = new JLabel("No one versions found in the server");
                        nofoursmallrectangleTextLabel.setForeground(defaultTextColor5);
                        Font sizednofourSmallrectangleTextLabel = font4.deriveFont(16.0F);
                        nofoursmallrectangleTextLabel.setFont(sizednofourSmallrectangleTextLabel);
                        nofoursmallrectangleTextLabel.setBounds(653, 149 + 81 * 3, 1000, 16);
                        mainScreen.add(nofoursmallrectangleTextLabel);
                        nofoursmallrectangleTextLabel.setVisible(false);

                        // No versions
                        JLabel nofivesmallrectangleTextLabel = new JLabel("No one versions found in the server");
                        nofivesmallrectangleTextLabel.setForeground(defaultTextColor5);
                        Font sizednofiveSmallrectangleTextLabel = font4.deriveFont(16.0F);
                        nofivesmallrectangleTextLabel.setFont(sizednofiveSmallrectangleTextLabel);
                        nofivesmallrectangleTextLabel.setBounds(653, 149 + 81 * 4, 1000, 16);
                        mainScreen.add(nofivesmallrectangleTextLabel);
                        nofivesmallrectangleTextLabel.setVisible(false);

                        // Version chooser
                        /*ImageIcon versionChooser = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/versionsChooser.png")));
                        JLabel versionChooserLabel = new JLabel(versionChooser);
                        versionChooserLabel.setBounds(634, 132, 353, 378);
                        mainScreen.add(versionChooserLabel);
                        versionChooserLabel.setVisible(false);*/

                        // White version chooser

                        /*ImageIcon whiteVersionChooser = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteVersionsChooser.png")));
                        JLabel whiteVersionChooserLabel = new JLabel(whiteVersionChooser);
                        whiteVersionChooserLabel.setBounds(634, 132, 353, 378);
                        mainScreen.add(whiteVersionChooserLabel);
                        whiteVersionChooserLabel.setVisible(false);*/

                        final boolean[] isVersionLabelClicked = {false};
                        final boolean[] isSettingsLabelClicked = {false};

                        /*versionLabel.addMouseListener(new MouseAdapter() {

                            boolean click = false;

                            AtomicReference<Float> currentBrightness = new AtomicReference<>(1.1F);

                            public void mouseEntered(MouseEvent e) {


                                mainScreen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                //versionLabel.setIcon(MainScreen.getBrighterIcon(version, 1.2F));
                            }

                            public void mouseExited(MouseEvent e) {
                                mainScreen.setCursor(Cursor.getDefaultCursor());
                                versionLabel.setIcon(MainScreen.getBrighterIcon(version, 1.0F));
                            }

                            public void mouseClicked(MouseEvent e) {

                                isVersionLabelClicked[0] = true;

                                int moveAmount = 20;
                                if (!click) {

                                    if (isSettingsLabelClicked[0]) {
                                        return;
                                    }

                                    click = true;
                                    activeContainer.active = true;

                                    // Add all components to mainScreen at the start of the animation


                                    if (makeWhiteTheme) {
                                        //whiteVersionChooserLabel.setVisible(true);
                                    } else {
                                        //versionChooserLabel.setVisible(true);
                                    }

                                    nosmallrectangleTextLabel.setVisible(true);
                                    notwosmallrectangleTextLabel.setVisible(true);
                                    nothreesmallrectangleTextLabel.setVisible(true);
                                    nofoursmallrectangleTextLabel.setVisible(true);
                                    nofivesmallrectangleTextLabel.setVisible(true);

                                    if (isSettingsLabelClicked[0]) {
                                        smallrectangleLabel.setVisible(false);

                                        if (makeWhiteTheme) {
                                            whiteSettingsrectangleLabel.setVisible(false);
                                            mainScreen.add(whiteSettingsrectangleLabel);
                                        } else {
                                            settingsrectangleLabel.setVisible(false);
                                            mainScreen.add(settingsrectangleLabel);
                                        }

                                        if (makeWhiteTheme) {
                                            mainScreen.remove(whiteOnLabel);
                                            mainScreen.remove(whiteOnUpdatesLabel);
                                            mainScreen.remove(whiteOffLabel);
                                        }

                                        mainScreen.remove(onLabel);
                                        mainScreen.remove(onUpdatesLabel);
                                        mainScreen.remove(offLabel);

                                        mainScreen.remove(settingsTextLabel);

                                        if (makeWhiteTheme) {
                                            mainScreen.remove(whiteSmallrectangleLabel);
                                        } else {
                                            mainScreen.remove(smallrectangleLabel);
                                        }

                                        mainScreen.remove(smallrectangleTextLabel);
                                        mainScreen.remove(smallrectangleTextLabel2);
                                        mainScreen.remove(smallrectangleTextLabel3);
                                        mainScreen.remove(smallrectangleTextLabel4);
                                        mainScreen.remove(smallrectangleTextLabel5);
                                        mainScreen.remove(smallrectangleTextLabel6);
                                    }

                                    mainScreen.repaint();

                                    // If Mac OS - delay 0, else - delay -2000
                                    int delay = System.getProperty("os.name").toLowerCase().contains("mac") ? 0 : -2000;

                                    Timer initialTimer = new Timer(delay, new ActionListener() {
                                        private int frameCount = 2;

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            int totalFrames = 50;
                                            if (frameCount < totalFrames) {
                                                float fraction = (float) frameCount / totalFrames;
                                                moveLabels((int) (fraction * moveAmount));
                                                frameCount++;
                                            } else {
                                                ((Timer) e.getSource()).stop();
                                            }
                                        }
                                    });
                                    initialTimer.setRepeats(true);
                                    initialTimer.start();
                                } else {



                                    isVersionLabelClicked[0] = false;

                                    click = false;
                                    activeContainer.active = false;

                                    int delay = System.getProperty("os.name").toLowerCase().contains("mactest") ? 0 : -2000;

                                    Timer timer = new Timer(delay, new ActionListener() {
                                        private int frameCount = 2;

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            int totalFrames = 50;
                                            if (frameCount < totalFrames) {
                                                float fraction = (float) frameCount / totalFrames;
                                                moveLabels((int) (fraction * -moveAmount));
                                                frameCount++;
                                            } else {
                                                ((Timer) e.getSource()).stop();

                                                // Remove all components from mainScreen after the animation
                                                cleanupAfterAnimation();
                                            }
                                        }
                                    });
                                    timer.setRepeats(true);
                                    timer.start();
                                }
                            }

                            private void moveLabels(int xOffset) {
                                mapLabel.setLocation(mapLabel.getX() + xOffset, mapLabel.getY());
                                mapTextLabel.setLocation(mapTextLabel.getX() + xOffset, mapTextLabel.getY());
                                newsLabel.setLocation(newsLabel.getX() + xOffset + xOffset / 4, newsLabel.getY());
                                newsTextLabel.setLocation(newsTextLabel.getX() + xOffset + xOffset / 4, newsTextLabel.getY());
                                go2Label.setLocation(go2Label.getX() + xOffset + xOffset / 4, go2Label.getY());
                                go3Label.setLocation(go3Label.getX() + xOffset, go3Label.getY());
                            }

                            private void cleanupAfterAnimation() {

                                //versionChooserLabel.setVisible(false);
                                versionsTextLabel.setVisible(false);

                                nosmallrectangleTextLabel.setVisible(false);
                                notwosmallrectangleTextLabel.setVisible(false);
                                nothreesmallrectangleTextLabel.setVisible(false);
                                nofoursmallrectangleTextLabel.setVisible(false);
                                nofivesmallrectangleTextLabel.setVisible(false);

                                if (makeWhiteTheme) {
                                    //whiteVersionChooserLabel.setVisible(false);
                                } else {
                                    //versionChooserLabel.setVisible(false);
                                }

                                mainScreen.repaint();
                            }

                        });*/

                        // Settings implementation 3rd step (animation and labels)
                        settingsLabel.addMouseListener(new MouseAdapter() {

                            boolean click = false;

                            public void mouseEntered(MouseEvent e) {
                                if (click) {
                                    settingsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                }
                                settingsLabel.setIcon(MainScreen.getBrighterIcon(settings, 1.4F));
                            }

                            public void mouseExited(MouseEvent e) {
                                settingsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                settingsLabel.setIcon(settings);
                            }

                            // Animation
                            public void mouseClicked(MouseEvent e) {

                                isSettingsLabelClicked[0] = true;

                                int moveAmount = 20;
                                if (!click) {
                                    click = true;
                                    activeContainer.active = true;

                                    // Add all components to mainScreen at the start of the animation

                                    if (isVersionLabelClicked[0]) {
                                        //versionChooserLabel.setVisible(false);
                                        versionsTextLabel.setVisible(false);

                                        nosmallrectangleTextLabel.setVisible(false);
                                        notwosmallrectangleTextLabel.setVisible(false);
                                        nothreesmallrectangleTextLabel.setVisible(false);
                                        nofoursmallrectangleTextLabel.setVisible(false);
                                        nofivesmallrectangleTextLabel.setVisible(false);
                                    }

                                    mainScreen.add(smallrectangleTextLabel);
                                    mainScreen.add(settingsTextLabel);

                                    if (makeWhiteTheme) {
                                        mainScreen.add(whiteOnUpdatesLabel);
                                        mainScreen.add(whiteOnLabel);
                                        mainScreen.add(whiteOffLabel);
                                    } else {
                                        mainScreen.add(onUpdatesLabel);
                                        mainScreen.add(onLabel);
                                        mainScreen.add(offLabel);
                                    }

                                    mainScreen.add(smallrectangleTextLabel2);
                                    mainScreen.add(smallrectangleTextLabel3);
                                    mainScreen.add(smallrectangleTextLabel4);
                                    mainScreen.add(smallrectangleTextLabel5);
                                    mainScreen.add(smallrectangleTextLabel6);

                                    if (makeWhiteTheme) {
                                        mainScreen.add(whiteSmallrectangleLabel);
                                    } else {
                                        mainScreen.add(smallrectangleLabel);
                                    }

                                    if (makeWhiteTheme) {
                                        mainScreen.add(whiteSettingsrectangleLabel);
                                        whiteSettingsrectangleLabel.setVisible(true);
                                    } else {
                                        mainScreen.add(settingsrectangleLabel);
                                        settingsrectangleLabel.setVisible(true);
                                    }
                                    smallrectangleLabel.setVisible(true);

                                    mainScreen.repaint();

                                    // If Mac OS - delay 0, else - delay -2000
                                    int delay = System.getProperty("os.name").toLowerCase().contains("mactest") ? 0 : -2000;

                                    Timer initialTimer = new Timer(delay, new ActionListener() {
                                        private int frameCount = 2;

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            int totalFrames = 50;
                                            if (frameCount < totalFrames) {
                                                float fraction = (float) frameCount / totalFrames;
                                                moveLabels((int) (fraction * moveAmount));
                                                frameCount++;
                                            } else {
                                                ((Timer) e.getSource()).stop();
                                            }
                                        }
                                    });
                                    initialTimer.setRepeats(true);
                                    initialTimer.start();
                                } else {

                                    click = false;
                                    activeContainer.active = false;

                                    isSettingsLabelClicked[0] = false;

                                    int delay = System.getProperty("os.name").toLowerCase().contains("mac") ? 0 : -2000;

                                    Timer timer = new Timer(delay, new ActionListener() {
                                        private int frameCount = 2;

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            int totalFrames = 50;
                                            if (frameCount < totalFrames) {
                                                float fraction = (float) frameCount / totalFrames;
                                                moveLabels((int) (fraction * -moveAmount));
                                                frameCount++;
                                            } else {
                                                ((Timer) e.getSource()).stop();

                                                // Remove all components from mainScreen after the animation
                                                cleanupAfterAnimation();
                                            }
                                        }
                                    });
                                    timer.setRepeats(true);
                                    timer.start();
                                }
                            }

                            private void moveLabels(int xOffset) {
                                mapLabel.setLocation(mapLabel.getX() + xOffset, mapLabel.getY());
                                mapTextLabel.setLocation(mapTextLabel.getX() + xOffset, mapTextLabel.getY());
                                newsLabel.setLocation(newsLabel.getX() + xOffset + xOffset / 4, newsLabel.getY());
                                newsTextLabel.setLocation(newsTextLabel.getX() + xOffset + xOffset / 4, newsTextLabel.getY());
                                go2Label.setLocation(go2Label.getX() + xOffset + xOffset / 4, go2Label.getY());
                                go3Label.setLocation(go3Label.getX() + xOffset, go3Label.getY());
                            }

                            private void cleanupAfterAnimation() {
                                smallrectangleLabel.setVisible(false);

                                if (makeWhiteTheme) {
                                    whiteSettingsrectangleLabel.setVisible(false);
                                    mainScreen.add(whiteSettingsrectangleLabel);
                                } else {
                                    settingsrectangleLabel.setVisible(false);
                                    mainScreen.add(settingsrectangleLabel);
                                }

                                if (makeWhiteTheme) {
                                    mainScreen.remove(whiteOnLabel);
                                    mainScreen.remove(whiteOnUpdatesLabel);
                                    mainScreen.remove(whiteOffLabel);
                                }

                                mainScreen.remove(onLabel);
                                mainScreen.remove(onUpdatesLabel);
                                mainScreen.remove(offLabel);

                                mainScreen.remove(settingsTextLabel);

                                if (makeWhiteTheme) {
                                    mainScreen.remove(whiteSmallrectangleLabel);
                                } else {
                                    mainScreen.remove(smallrectangleLabel);
                                }

                                mainScreen.remove(smallrectangleTextLabel);
                                mainScreen.remove(smallrectangleTextLabel2);
                                mainScreen.remove(smallrectangleTextLabel3);
                                mainScreen.remove(smallrectangleTextLabel4);
                                mainScreen.remove(smallrectangleTextLabel5);
                                mainScreen.remove(smallrectangleTextLabel6);

                                mainScreen.repaint();
                            }
                        });

                // Go & Play buttons
                ImageIcon go = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/go.png")));
                JLabel goLabel = new JLabel(go);
                goLabel.setBounds(530, 229, 10, 18);
                mainScreen.add(goLabel);

                final ImageIcon play = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/play.png")));
                final JLabel playLabel = new JLabel(play);
                playLabel.setBounds(358, 213, 198, 50);
                mainScreen.add(playLabel);

                boolean swiftPlay = false;

                // Play (starting game)
                playLabel.addMouseListener(new MouseAdapter() {
                    AtomicReference<Float> currentBrightness = new AtomicReference<>(1.1F);
                    private boolean clicked = false;

                    public void mouseEntered(MouseEvent e) {
                        mainScreen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        if (!clicked) {
                            playLabel.setIcon(MainScreen.getBrighterIcon(play, currentBrightness.get()));
                        }
                    }

                    public void mouseExited(MouseEvent e) {
                        mainScreen.setCursor(Cursor.getDefaultCursor());
                        if (!clicked) {
                            playLabel.setIcon(play);
                        } else if (currentBrightness.get() == 0.8F) {
                            playLabel.setIcon(MainScreen.getBrighterIcon(play, currentBrightness.get()));
                        }
                    }

                    final AtomicReference<Boolean> scheduled = new AtomicReference<>(true);

                    public void mouseClicked(MouseEvent e) {
                        currentBrightness.set(0.8F);
                        clicked = true;

                        // TODO: Start
                        playTextLabel.setText("Starting...");
                        playTextLabel.paintImmediately(playTextLabel.getVisibleRect());

                        // Hard task, so we need to do it in another thread to avoid freezing
                        CompletableFuture.runAsync(() -> {
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                        });

                        Downloader.launch(enteredNickname, playTextLabel, currentBrightness, swiftPlay, makeOptimization, makeFreecam, xmx);

                        scheduled.set(true);

                        ScheduledExecutorService scheduler;

                        if (scheduled.get()) {
                            scheduler = Executors.newScheduledThreadPool(1);

                            final ScheduledExecutorService finalScheduler = scheduler;

                            Runnable brightnessTask = () -> {
                                if (currentBrightness.get() == 0.8F) {
                                    clicked = false;
                                    currentBrightness.set(1.0F);
                                } else if (currentBrightness.get() == 1.11F) {
                                    finalScheduler.shutdown();
                                    currentBrightness = new AtomicReference<>(1.1F);
                                    playLabel.setIcon(MainScreen.getBrighterIcon(play, currentBrightness.get()));
                                } else {
                                    currentBrightness.set(0.8F);
                                }

                                SwingUtilities.invokeLater(() -> {
                                    Icon brighterIcon = getBrighterIcon(play, currentBrightness.get());
                                    playLabel.setIcon(brighterIcon);
                                });
                            };

                            scheduler.scheduleAtFixedRate(brightnessTask, 0, 1, TimeUnit.SECONDS);
                        }
                    }
                });

                // Test Swift-play
                final ImageIcon play2 = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/swiftplay.png")));
                final JLabel playLabel2 = new JLabel(play2);
                playLabel2.setBounds(107, 213, 233, 50);
                //mainScreen.add(playLabel2);
                playLabel2.addMouseListener(new MouseAdapter() {
                    final AtomicReference<Float> currentBrightness2 = new AtomicReference<>(1.1F);
                    private final boolean clicked2 = false;

                    public void mouseEntered(MouseEvent e) {
                        mainScreen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        if (!clicked2) {
                            playLabel2.setIcon(MainScreen.getBrighterIcon(play2, currentBrightness2.get()));
                        }
                    }

                    public void mouseExited(MouseEvent e) {
                        mainScreen.setCursor(Cursor.getDefaultCursor());
                        if (!clicked2) {
                            playLabel2.setIcon(play2);
                        } else if (currentBrightness2.get() == 0.8F) {
                            playLabel2.setIcon(MainScreen.getBrighterIcon(play2, currentBrightness2.get()));
                        }
                    }

                    //// Самый лучший код который я видел в жизни - это пиздец - я такой смотрю и WHAT THE FUCK????????
                    // КОЛЯ, ЭТО РЕАЛЬНО НОРМ КОД ТЫ ПРОСТО НЕ ПОНИМАЕШЬ
                    // Запускать приложение???
                    // - КАК ХОЧЕШЬ
                    // Щас тогда
                    // Блять. Краш какой-то. Коля, ну это уже твоя хуйня


                    // Indev
                    // TODO: Swift-play
                    public void mouseClicked(MouseEvent e) {
                        //currentBrightness2.set(0.9F);
                        //clicked2 = true;
                        //boolean swiftPlay = true;

                        //playTextLabel2.paintImmediately(playTextLabel2.getVisibleRect());

                        // Hard task, so we need to do it in another thread to avoid freezing
                        /*CompletableFuture.runAsync(() -> {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                        }); */

                        /*
                        Download.launch(enteredNickname, playTextLabel2, currentBrightness2, swiftPlay, makeOptimization, makeFreecam);
                        scheduled2.set(true);
                        ScheduledExecutorService scheduler2 = null;
                        */

                        /*if (scheduled2.get()) {
                            scheduler2 = Executors.newScheduledThreadPool(1);

                            final ScheduledExecutorService finalScheduler2 = scheduler2;

                            Runnable brightnessTask2 = () -> {
                                if (currentBrightness2.get() == 0.9F) {
                                    clicked2 = false;
                                    currentBrightness2.set(1.1F);
                                } else if (currentBrightness2.get() == 1.11F) {
                                    finalScheduler2.shutdown();
                                    currentBrightness2 = new AtomicReference<>(1.1F);
                                    playLabel2.setIcon(MainScreen.getBrighterIcon(play2, currentBrightness2.get()));
                                } else {
                                    currentBrightness2.set(0.9F);
                                }

                                SwingUtilities.invokeLater(() -> {
                                    Icon brighterIcon = getBrighterIcon(play2, currentBrightness2.get());
                                    playLabel2.setIcon(brighterIcon);
                                });
                            };*/

                        //scheduler2.scheduleAtFixedRate(brightnessTask2, 0, 1, TimeUnit.SECONDS);
                        //}
                    }
                });

                Font sizedFont = font1.deriveFont(26.0F);
                launcherText.setFont(sizedFont);
                launcherText.setBounds(322, -245, 2000, 600);
                mainScreen.add(launcherText);

                // Changing nickname
                final ImageIcon changeNicknameIcon = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/changeNickname.png")));
                final JLabel changeNicknameIconLabel = new JLabel(changeNicknameIcon);
                Dimension changeNicknameIconSize = changeNicknameIconLabel.getPreferredSize();
                changeNicknameIconLabel.setBounds(522, 147, changeNicknameIconSize.width, changeNicknameIconSize.height);
                changeNicknameIconLabel.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        mainScreen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        changeNicknameIconLabel.setIcon(MainScreen.getBrighterIcon(changeNicknameIcon, 1.4F));
                    }

                    public void mouseExited(MouseEvent e) {
                        mainScreen.setCursor(Cursor.getDefaultCursor());
                        changeNicknameIconLabel.setIcon(changeNicknameIcon);
                    }

                    public void mouseClicked(MouseEvent e) {
                        // TODO: rewrite DreamLauncher.config.nickName
                        // DreamLauncher.config.nickName = null;
                        // DreamLauncher.saveConfig(DreamLauncher.config);
                        DreamLauncher launcher = new DreamLauncher();
                        //launcher.center();
                        DreamLauncher.main(new String[0]);
                        System.out.println("Auto-fill file deleted.");

                        mainScreen.dispose();
                    }
                });
                mainScreen.add(changeNicknameIconLabel);

                // Player's Head
                JLabel nicknameLabel;
                try {
                    URL headURL = new URL("https://new.frogdream.xyz/getUserHead/" + enteredNickname + ".png");
                    Image headImage = ImageIO.read(headURL);
                    headImage = headImage.getScaledInstance(32, 32, 4);
                    ImageIcon head = new ImageIcon(headImage);
                    nicknameLabel = new JLabel(head);
                    nicknameLabel.setBounds(122, 140, 32, 32);
                    mainScreen.add(nicknameLabel);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Nickname
                nicknameLabel = new JLabel(enteredNickname);
                nicknameLabel.setForeground(defaultTextColor);
                Font sizedFontOfNickname = font2.deriveFont(16.0F);
                nicknameLabel.setFont(sizedFontOfNickname);
                nicknameLabel.setBounds(169, 148, 500, 16);
                mainScreen.add(nicknameLabel);

                ImageIcon rectangle = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/rectangle.png")));
                JLabel rectangleLabel = new JLabel(rectangle);
                Dimension rectangleSize = rectangleLabel.getPreferredSize();
                rectangleLabel.setBounds(107, 131, rectangleSize.width, rectangleSize.height);

                ImageIcon whiteRectangle = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteRectangle.png")));
                JLabel whiteRectangleLabel = new JLabel(whiteRectangle);
                Dimension whiteRectangleSize = whiteRectangleLabel.getPreferredSize();
                whiteRectangleLabel.setBounds(107, 131, whiteRectangleSize.width, whiteRectangleSize.height);

                if (makeWhiteTheme) {
                    mainScreen.add(whiteRectangleLabel);
                } else {
                    mainScreen.add(rectangleLabel);
                }

                // Folder
                final ImageIcon folder = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/folder.png")));
                final JLabel folderLabel = new JLabel(folder);
                folderLabel.setBounds(24, 33, 24, 24);
                mainScreen.add(folderLabel);
                folderLabel.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        folderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        folderLabel.setIcon(MainScreen.getBrighterIcon(folder, 1.4F));
                    }

                    public void mouseExited(MouseEvent e) {
                        folderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        folderLabel.setIcon(folder);
                    }

                    public void mouseClicked(MouseEvent e) {
                        MainScreen.openDownloadsFolder();
                    }
                });

                // Menu
                ImageIcon menu = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/menu.png")));
                JLabel menuLabel = new JLabel(menu);
                Dimension menuSize = menuLabel.getPreferredSize();
                menuLabel.setBounds(0, 0, menuSize.width, menuSize.height);

                ImageIcon whiteMenu = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/whiteMenu.png")));
                JLabel whiteMenuLabel = new JLabel(whiteMenu);
                Dimension whiteMenuSize = whiteMenuLabel.getPreferredSize();
                whiteMenuLabel.setBounds(0, 0, whiteMenuSize.width, whiteMenuSize.height);

                if (makeWhiteTheme) {
                    mainScreen.add(whiteMenuLabel);
                } else {
                    mainScreen.add(menuLabel);
                }

                if (makeWhiteTheme) {
                    onLabel.setVisible(false);
                    offLabel.setVisible(false);
                    onUpdatesLabel.setVisible(false);
                }

                mainScreen.add(mapLabel);
                mainScreen.add(newsLabel);

                whiteSettingsrectangleLabel.setVisible(false);
                settingsrectangleLabel.setVisible(false);

                mainScreen.setVisible(true);
                mainScreen.repaint();
            });
        }
    }

    // Bright
    private static ImageIcon getBrighterIcon(ImageIcon icon, float brightness) {
        Image img = icon.getImage();
        BufferedImage bufferedImage = new BufferedImage(
                img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(img, 0, 0, null);
        graphics.dispose();

        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int rgb = bufferedImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                r = Math.min((int) (r * brightness), 255);
                g = Math.min((int) (g * brightness), 255);
                b = Math.min((int) (b * brightness), 255);

                bufferedImage.setRGB(x, y, (rgb & 0xFF000000) | (r << 16) | (g << 8) | b);
            }
        }

        return new ImageIcon(bufferedImage);
    }

    // Work with files
    private static void saveBooleanValueToFile(String fileName, boolean value) {
        String appDataPath = getAppDataPath();

        String filePath = appDataPath + fileName;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(Boolean.toString(value));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean readBooleanValueFromFile(String filePath) {

        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();

                return Boolean.parseBoolean(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static void saveValueToFile(String filePath, String value) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readValueFromFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

                return reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}