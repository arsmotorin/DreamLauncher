package xyz.frogdream.launcher;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import javax.swing.*;

import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import xyz.frogdream.launcher.downloader.Download;

// This is a main page of launcher. There are functional buttons, images
// (news and map) and game launch.

// Obey rules and features of design, that cubelius put in /design folder,
// cause this class have main design parts of code.

// Obey rule of comments. Write comments only on English.

public class MainScreen extends JFrame {
    MainScreen() {

        // Fixed screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int frameWidth = this.getWidth();
        int frameHeight = this.getHeight();

        int x = (screenWidth - frameWidth) / 7;
        int y = (screenHeight - frameHeight) / 7;
        this.setLocation(x, y);

        // Check for updates
        this.checkGithubRelease();
    }

    private void checkGithubRelease() {

        // Version changing. Change version only like that: v1.0.1, v1.1.4, etc. All pull requests

        // v1. - main version
        // v1.0 - just version with smth minor changes
        // v1.0.1 - version with bug fixes

        // that have bad type of version will be declined.

        String currentVersion = "v1.0.0";

        MainScreen.GithubReleaseChecker releaseChecker = new MainScreen.GithubReleaseChecker();
        releaseChecker.checkGithubRelease("Frogdream", "Launcher", currentVersion);
    }

    // Open download folder
    private static void openDownloadsFolder() {
        String osName = System.getProperty("os.name").toLowerCase();
        String userHomeDirectory;
        if (osName.contains("win")) {
            userHomeDirectory = System.getenv("LOCALAPPDATA") + "\\.FrogDream";
        } else if (osName.contains("mac")) {
            userHomeDirectory = System.getenv("LOCALAPPDATA") + "/.FrogDream";
        } else {
            if (!osName.contains("linux")) {
                JOptionPane.showMessageDialog(null, "Ваша система не поддерживает открытие папки с игрой. \nПожалуйста, откройте баг-репорт и напишите, какую систему вы используете.");
                return;
            }

            userHomeDirectory = System.getenv("LOCALAPPDATA") + "/FrogDreamCache";
        }

        File file = new File(userHomeDirectory);

        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "Папка с игрой не найдена. Скорее всего, вы только установили лаунчер, но не нажали на кнопку \"Играть\".");
            return;
        }

        try {
            Desktop.getDesktop().open(file);
        } catch (IOException var4) {
            throw new RuntimeException(var4);
        }
    }

    // GitHub release checker
    public static class GithubReleaseChecker {
        public void checkGithubRelease(String repoOwner, String repoName, String currentVersion) {
            String url = String.format("https://api.github.com/repos/%s/%s/releases", repoOwner, repoName);

            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection)apiUrl.openConnection();
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();

                    String line;
                    while((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    JSONArray releases = new JSONArray(response.toString());

                    for(int i = 0; i < releases.length(); ++i) {
                        JSONObject release = releases.getJSONObject(i);
                        String tagName = release.getString("tag_name");
                        if (this.isNewerVersion(tagName, currentVersion)) {
                            System.out.println("Доступна новая версия лаунчера!");
                            int option = JOptionPane.showOptionDialog(null, "Доступно обновление лаунчера!", "Обновление лаунчера", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Потом обновлю", "Обновить"}, "Обновить");
                            if (option == 1) {
                                Desktop.getDesktop().browse(new URI("https://github.com/Frogdream/Launcher/releases"));
                            }

                            return;
                        }
                    }

                    System.out.println("Установлена последняя версия лаунчера.");
                } else {
                    System.out.println("Не удалось получить данные о релизах.");
                }
            } catch (JSONException | IOException var15) {
                var15.printStackTrace();
            } catch (URISyntaxException var16) {
                throw new RuntimeException(var16);
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
            for(int i = 0; i < minLength; ++i) {
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
            mainScreen.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainScreen.setTitle("Frogdream Launcher");
            mainScreen.setSize(1022, 600); //
            mainScreen.getContentPane().setBackground(new Color(12, 12, 12));
            mainScreen.setLayout(null);
            mainScreen.setResizable(false);
            JLabel launcherText = new JLabel("Launcher");
            launcherText.setForeground(new Color(154, 189, 57));
            InputStream is = FrogdreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Bold.ttf");

            Font font;
            try {
                assert is != null;

                font = Font.createFont(0, is);
            } catch (IOException | FontFormatException var56) {
                throw new RuntimeException(var56);
            }

            Color defaultTextColor2 = new Color(255, 255, 255);
            InputStream is3 = FrogdreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Medium.ttf");

            Font font3;
            try {
                assert is3 != null;

                font3 = Font.createFont(0, is3);
            } catch (IOException | FontFormatException var55) {
                throw new RuntimeException(var55);
            }

            JLabel playTextLabel = new JLabel("Играть");
            playTextLabel.setForeground(defaultTextColor2);

            Font sizedFontForPlayText = font3.deriveFont(16.0F);
            playTextLabel.setFont(sizedFontForPlayText);
            playTextLabel.setBounds(298, 188, 100, 100);

            mainScreen.add(playTextLabel);

            ImageIcon logo = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/smallLogo.png")));
            JLabel logoLabel = new JLabel(logo);
            logoLabel.setBounds(145, 20, 75, 75);

            mainScreen.add(logoLabel);

            final ImageIcon wiki = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/wiki.png")));
            final JLabel wikiLabel = new JLabel(wiki);
            wikiLabel.setBounds(66, 517, 24, 24);

            mainScreen.add(wikiLabel);

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
                    } catch (URISyntaxException | IOException var3) {
                        var3.printStackTrace();
                    }

                }
            });
            final ImageIcon updateLauncher = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/updateLauncher.png")));
            final JLabel updateLauncherLabel = new JLabel(updateLauncher);
            updateLauncherLabel.setBounds(99, 517, 24, 24);
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
                    } catch (URISyntaxException | IOException var3) {
                        var3.printStackTrace();
                    }

                }
            });
            final ImageIcon premium = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/premium.png")));
            final JLabel premiumLabel = new JLabel(premium);
            premiumLabel.setBounds(133, 517, 24, 24);
            mainScreen.add(premiumLabel);
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
                        Desktop.getDesktop().browse(new URI("https://www.frogdream.xyz/premium"));
                    } catch (URISyntaxException | IOException var3) {
                        var3.printStackTrace();
                    }

                }
            });
            final ImageIcon friend = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/friend.png")));
            final JLabel friendLabel = new JLabel(friend);
            friendLabel.setBounds(167, 517, 24, 24);
            mainScreen.add(friendLabel);
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
            ImageIcon go = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/go.png")));
            JLabel goLabel = new JLabel(go);
            goLabel.setBounds(451, 229, 10, 18);
            mainScreen.add(goLabel);
            final ImageIcon play = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/play.png")));
            final JLabel playLabel = new JLabel(play);
            playLabel.setBounds(279, 213, 198, 50);
            mainScreen.add(playLabel);
            playLabel.addMouseListener(new MouseAdapter() {
                private float currentBrightness = 1.1F;
                private boolean clicked = false;

                public void mouseEntered(MouseEvent e) {
                    mainScreen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if (!clicked) {
                        playLabel.setIcon(MainScreen.getBrighterIcon(play, currentBrightness));
                    }
                }

                // Monitoring
                public void mouseExited(MouseEvent e) {
                    mainScreen.setCursor(Cursor.getDefaultCursor());
                    if (!clicked) {
                        playLabel.setIcon(play);
                    } else if (currentBrightness == 0.8F) {
                        playLabel.setIcon(MainScreen.getBrighterIcon(play, currentBrightness));
                    }
                }

                public void mouseClicked(MouseEvent e) {
                    currentBrightness = 0.8F;
                    clicked = true;

                    // TODO: Start
                    playTextLabel.setText("Загрузка...");
                    playTextLabel.paintImmediately(playTextLabel.getVisibleRect());

                    Download.launch(enteredNickname);

                    // After launch
                    mainScreen.dispose();

                    CompletableFuture.runAsync(() -> {
                        Icon brighterIcon = getBrighterIcon(play, currentBrightness);
                        playLabel.setIcon(brighterIcon);
                    });
                }
            });
            Font sizedFont = font.deriveFont(26.0F);
            launcherText.setFont(sizedFont);
            launcherText.setBounds(243, -245, 2000, 600);

            mainScreen.add(launcherText);

            final ImageIcon changeNicknameIcon = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/changeNickname.png")));
            final JLabel changeNicknameIconLabel = new JLabel(changeNicknameIcon);
            Dimension changeNicknameIconSize = changeNicknameIconLabel.getPreferredSize();
            changeNicknameIconLabel.setBounds(443, 147, changeNicknameIconSize.width, changeNicknameIconSize.height);
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
                    FrogdreamLauncher.config.nickName = null;
                    FrogdreamLauncher.saveConfig(FrogdreamLauncher.config);
                    FrogdreamLauncher launcher = new FrogdreamLauncher();
                    launcher.center();
                    FrogdreamLauncher.main(new String[0]);
                    System.out.println("Файл авто-заполнения ника успешно удалён.");

                    mainScreen.dispose();
                }
            });
            mainScreen.add(changeNicknameIconLabel);

            JLabel nicknameLabel;
            try {
                URL headURL = new URL("https://new.frogdream.xyz/getUserHead/" + enteredNickname + ".png");
                Image headImage = ImageIO.read(headURL);
                headImage = headImage.getScaledInstance(32, 32, 4);
                ImageIcon head = new ImageIcon(headImage);
                nicknameLabel = new JLabel(head);
                nicknameLabel.setBounds(43, 140, 32, 32);
                mainScreen.add(nicknameLabel);
            } catch (IOException var53) {
                var53.printStackTrace();
            }

            InputStream is2 = FrogdreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Medium.ttf");
            Color defaultTextColor = new Color(99, 99, 99);

            Font font2;
            try {
                assert is2 != null;

                font2 = Font.createFont(0, is2);
            } catch (IOException | FontFormatException var54) {
                throw new RuntimeException(var54);
            }

            // Nickname
            nicknameLabel = new JLabel(enteredNickname);
            nicknameLabel.setForeground(defaultTextColor);
            Font sizedFontOfNickname = font2.deriveFont(16.0F);
            nicknameLabel.setFont(sizedFontOfNickname);
            nicknameLabel.setBounds(90, 148, 500, 16);
            mainScreen.add(nicknameLabel);

            // Go2/3 for pictures
            ImageIcon go2 = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/go.png")));
            JLabel go2Label = new JLabel(go2);
            go2Label.setBounds(960, 243, 10, 18);
            mainScreen.add(go2Label);
            ImageIcon go3 = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/go.png")));
            JLabel go3Label = new JLabel(go3);
            go3Label.setBounds(960, 508, 10, 18);
            mainScreen.add(go3Label);

            // News
            JLabel newsTextLabel = new JLabel("Последние новости");
            newsTextLabel.setForeground(defaultTextColor2);
            Font sizedFontNewsTextLabel = font3.deriveFont(16.0F);
            newsTextLabel.setFont(sizedFontNewsTextLabel);
            newsTextLabel.setBounds(793, 200, 1000, 100);
            mainScreen.add(newsTextLabel);

            // Map
            JLabel mapTextLabel = new JLabel("Карта");
            mapTextLabel.setForeground(defaultTextColor2);
            Font sizedMapTextLabel = font3.deriveFont(16.0F);
            mapTextLabel.setFont(sizedMapTextLabel);
            mapTextLabel.setBounds(902, 508, 1000, 16);
            mainScreen.add(mapTextLabel);

            // Rectangle
            ImageIcon rectangle = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/rectangle.png")));
            JLabel rectangleLabel = new JLabel(rectangle);
            Dimension rectangleSize = rectangleLabel.getPreferredSize();
            rectangleLabel.setBounds(28, 131, rectangleSize.width, rectangleSize.height);
            mainScreen.add(rectangleLabel);

            // Divider
            ImageIcon divider = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/divider.png")));
            JLabel dividerLabel = new JLabel(divider);
            Dimension dividerSize = dividerLabel.getPreferredSize();
            dividerLabel.setBounds(512, 0, dividerSize.width, dividerSize.height);
            mainScreen.add(dividerLabel);

            // News link
            ImageIcon news = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/news.png")));
            JLabel newsLabel = new JLabel(news);
            newsLabel.setBounds(542, 24, 449, 250);
            MainScreen.animation2(go2Label, newsTextLabel, newsLabel);
            newsLabel.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    mainScreen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                public void mouseExited(MouseEvent e) {
                    mainScreen.setCursor(Cursor.getDefaultCursor());
                }
            });
            mainScreen.add(newsLabel);

            // Map link
            ImageIcon map = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/map.png")));
            JLabel mapLabel = new JLabel(map);
            mapLabel.setBounds(542, 291, 449, 250);
            mainScreen.add(mapLabel);

            // Animation and cursor
            MainScreen.animation(go3Label, mapTextLabel, mapLabel);
            mapLabel.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    mainScreen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                public void mouseExited(MouseEvent e) {
                    mainScreen.setCursor(Cursor.getDefaultCursor());
                }
            });

            // Buttons
            final ImageIcon folder = new ImageIcon(Objects.requireNonNull(MainScreenInitializer.class.getResource("/Images/folder.png")));
            final JLabel folderLabel = new JLabel(folder);
            folderLabel.setBounds(31, 517, 24, 24);
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
            mainScreen.setVisible(true);
        }
    }

    // Animation
    private static void animation(JLabel go3Label, JLabel mapTextLabel, JLabel mapLabel) {
        int initialMapLabelY = mapLabel.getY();
        int initialMapTextLabelY = mapTextLabel.getY();
        int initialGo3LabelY = go3Label.getY();

        int animationDuration = 500;
        int animationSteps = 20;
        int animationDelay = animationDuration / animationSteps; // Delay
        int maxDeltaY = 5;

        final Timer[] timer = {null};
        final int[] deltaY = {0};

        mapLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://map.frogdream.xyz"));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (timer[0] != null && e.getSource() == mapLabel) {
                    timer[0].stop();
                    mapLabel.setLocation(mapLabel.getX(), initialMapLabelY);
                    mapTextLabel.setLocation(mapTextLabel.getX(), initialMapTextLabelY);
                    go3Label.setLocation(go3Label.getX(), initialGo3LabelY);
                    deltaY[0] = 0; // Reset
                    mapLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        });

        mapLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mapLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                if (timer[0] != null && timer[0].isRunning()) {
                    timer[0].restart(); // Reset
                } else {
                    timer[0] = new Timer(animationDelay, actionEvent -> {
                        if (deltaY[0] >= maxDeltaY) {
                            timer[0].stop();
                            return;
                        }
                        mapLabel.setLocation(mapLabel.getX(), mapLabel.getY() - 1);
                        mapTextLabel.setLocation(mapTextLabel.getX(), mapTextLabel.getY() - 1);
                        go3Label.setLocation(go3Label.getX(), go3Label.getY() - 1);
                        deltaY[0]++;
                    });
                    timer[0].setInitialDelay(100);
                    timer[0].start();
                }
            }
        });
    }

    private static void animation2(JLabel go2Label, JLabel newsTextLabel, JLabel newsLabel) {
        int initialNewsLabelY = newsLabel.getY();
        int initialNewsTextLabelY = newsTextLabel.getY();
        int initialGo2LabelY = go2Label.getY();

        int animationDuration = 500;
        int animationSteps = 20;
        int animationDelay = animationDuration / animationSteps; // Delay
        int maxDeltaY = 5;

        final Timer[] timer = {null};
        final int[] deltaY = {0};

        newsLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseExited(MouseEvent e) {
                if (timer[0] != null && e.getSource() == newsLabel) {
                    timer[0].stop();
                    newsLabel.setLocation(newsLabel.getX(), initialNewsLabelY);
                    newsTextLabel.setLocation(newsTextLabel.getX(), initialNewsTextLabelY);
                    go2Label.setLocation(go2Label.getX(), initialGo2LabelY);
                    deltaY[0] = 0; // Reset
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://vk.com/frogdream"));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });

        newsLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (timer[0] != null && timer[0].isRunning()) {
                    timer[0].restart(); // Reset
                } else {
                    timer[0] = new Timer(animationDelay, actionEvent -> {
                        if (deltaY[0] >= maxDeltaY) {
                            timer[0].stop();
                            return;
                        }
                        newsLabel.setLocation(newsLabel.getX(), newsLabel.getY() - 1);
                        newsTextLabel.setLocation(newsTextLabel.getX(), newsTextLabel.getY() - 1);
                        go2Label.setLocation(go2Label.getX(), go2Label.getY() - 1);
                        deltaY[0]++;
                    });
                    timer[0].setInitialDelay(100);
                    timer[0].start();
                }
            }
        });
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
}
