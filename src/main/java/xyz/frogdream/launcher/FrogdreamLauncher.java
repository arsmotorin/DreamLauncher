package xyz.frogdream.launcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import xyz.frogdream.launcher.MainScreen.MainScreenInitializer;

public class FrogdreamLauncher extends JFrame {
    private static boolean isTextChanged = false;
    private static JLabel enterLabel;
    private static JTextField nickname;
    private static FrogdreamLauncher display;

    FrogdreamLauncher() {
        this.center();
        ImageIcon logo = new ImageIcon((URL)Objects.requireNonNull(this.getClass().getResource("/Images/logo.png")));
        JLabel logoLabel = new JLabel(logo);
        Dimension size = logoLabel.getPreferredSize();
        logoLabel.setBounds(300, 203, size.width, size.height);
        this.add(logoLabel);
    }

    public void center() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int frameWidth = this.getWidth();
        int frameHeight = this.getHeight();
        int x = (screenWidth - frameWidth) / 7;
        int y = (screenHeight - frameHeight) / 7;
        this.setLocation(x, y);
    }

    public static void main(String[] args) {
        display = new FrogdreamLauncher();
        String folderPath = System.getenv("LOCALAPPDATA") + "/FrogDreamCache";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
            } else {
                return;
            }
        }
        String filePath = folderPath + "/autofill.txt";
        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("Сработало авто-заполнение, переход на главный экран.");

            try {
                BufferedReader reader = new BufferedReader(new FileReader(filePath));

                try {
                    StringBuilder content = new StringBuilder();

                    while(true) {
                        String line;
                        if ((line = reader.readLine()) == null) {
                            String enteredNickname = content.toString();
                            MainScreen mainscreen = new MainScreen();
                            MainScreenInitializer.initialize(mainscreen, enteredNickname);
                            mainscreen.setVisible(true);
                            display.setVisible(false);
                            display.dispose();
                            break;
                        }

                        content.append(line);
                    }
                } catch (Throwable var18) {
                    try {
                        reader.close();
                    } catch (Throwable var15) {
                        var18.addSuppressed(var15);
                    }

                    throw var18;
                }

                reader.close();
                return;
            } catch (IOException var19) {
                var19.printStackTrace();
            }
        } else {
            System.out.println("Ошибка в авто-заполнении.");
        }

        display.setDefaultCloseOperation(3);
        display.setTitle("Frogdream Launcher");
        display.setSize(1022, 589);
        display.getContentPane().setBackground(new Color(12, 12, 12));
        display.setLayout((LayoutManager)null);
        JLabel launcherText = new JLabel("Launcher");
        launcherText.setForeground(new Color(154, 189, 57));
        InputStream is = FrogdreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Bold.ttf");

        try {
            assert is != null;

            Font font = Font.createFont(0, is);
            Font sizedFont = font.deriveFont(56.0F);
            launcherText.setFont(sizedFont);
            launcherText.setBounds(478, -19, 2000, 600);
            display.add(launcherText);
        } catch (IOException | FontFormatException var17) {
            throw new RuntimeException(var17);
        }

        nickname = new JTextField();
        InputStream nicknameFontStream = FrogdreamLauncher.class.getResourceAsStream("/Fonts/GolosText-Medium.ttf");

        Font nicknameFont;
        try {
            assert nicknameFontStream != null;

            nicknameFont = Font.createFont(0, nicknameFontStream);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(nicknameFont);
        } catch (IOException | FontFormatException var16) {
            throw new RuntimeException(var16);
        }

        Font sizedNicknameFont = nicknameFont.deriveFont(16.0F);
        nickname.setFont(sizedNicknameFont);
        nickname.setText("Ник");
        nickname.setBorder((Border)null);
        nickname.setOpaque(false);

        Color defaultTextColor = new Color(100, 101, 101);

        nickname.setForeground(defaultTextColor);
        nickname.setBounds(315, 375, 385, 60);
        nickname.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!FrogdreamLauncher.isTextChanged) {
                    FrogdreamLauncher.nickname.setText("");
                    FrogdreamLauncher.isTextChanged = true;
                }

            }
        });
        ImageIcon rectangle = new ImageIcon((URL)Objects.requireNonNull(FrogdreamLauncher.class.getResource("/Images/rectangle.png")));
        JLabel rectangleLabel = new JLabel(rectangle);
        Dimension rectangleSize = rectangleLabel.getPreferredSize();
        rectangleLabel.setBounds(293, 380, rectangleSize.width, rectangleSize.height);
        final ImageIcon enter = new ImageIcon((URL)Objects.requireNonNull(FrogdreamLauncher.class.getResource("/Images/enter.png")));
        enterLabel = new JLabel(enter);
        enterLabel.setBounds(704, 390, 30, 30);
        enterLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                FrogdreamLauncher.enterLabel.setIcon(FrogdreamLauncher.getBrighterIcon(enter));
            }

            public void mouseExited(MouseEvent e) {
                FrogdreamLauncher.enterLabel.setIcon(enter);
            }

            public void mouseClicked(MouseEvent e) {
                FrogdreamLauncher.performAction();
            }
        });
        KeyListener enterKeyListener = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    FrogdreamLauncher.performAction();
                }

            }
        };
        nickname.addKeyListener(enterKeyListener);
        display.add(enterLabel);
        display.add(nickname);
        display.add(rectangleLabel);
        display.setVisible(true);
        display.setResizable(false);
    }

    private static void performAction() {
        String enteredNickname = nickname.getText();
        if (enteredNickname.equals("cubelius") || enteredNickname.equals("Redmor") || enteredNickname.equals("Kolyakot33")) {
            ImageApp.main();
        }

        Database.main(enteredNickname);
        if (Database.statusOfKey) {
            MainScreen mainscreen = new MainScreen();
            MainScreenInitializer.initialize(mainscreen, enteredNickname);
            mainscreen.setVisible(true);
            display.setVisible(false);
        }

        String folderPath = System.getenv("LOCALAPPDATA") + "/FrogDreamCache";

        String filePath = folderPath + "/autofill.txt";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            try {
                writer.write(enteredNickname);
                System.out.println("Авто-заполнение ника включено.");
            } catch (Throwable var6) {
                try {
                    writer.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }
                throw var6;
            }
            writer.close();
        } catch (IOException var7) {
            var7.printStackTrace();
        }
    }

    private static ImageIcon getBrighterIcon(ImageIcon icon) {
        Image img = icon.getImage();
        BufferedImage bufferedImage = new BufferedImage(img.getWidth((ImageObserver)null), img.getHeight((ImageObserver)null), 2);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(img, 0, 0, (ImageObserver)null);
        graphics.dispose();

        for(int y = 0; y < bufferedImage.getHeight(); ++y) {
            for(int x = 0; x < bufferedImage.getWidth(); ++x) {
                int rgb = bufferedImage.getRGB(x, y);
                int r = rgb >> 16 & 255;
                int g = rgb >> 8 & 255;
                int b = rgb & 255;
                r = Math.min((int)((float)r * 1.4F), 255);
                g = Math.min((int)((float)g * 1.4F), 255);
                b = Math.min((int)((float)b * 1.4F), 255);
                bufferedImage.setRGB(x, y, rgb & -16777216 | r << 16 | g << 8 | b);
            }
        }

        return new ImageIcon(bufferedImage);
    }
}
