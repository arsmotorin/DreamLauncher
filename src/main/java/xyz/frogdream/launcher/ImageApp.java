package xyz.frogdream.launcher;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class ImageApp {
    public static void main() {
        final JFrame frame = new JFrame("AHAHAHAHAHAHHAHAHAHAHHAHAHAHA");
        frame.getContentPane().setLayout(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1284, 982);

        ImageIcon icon = new ImageIcon(Objects.requireNonNull(ImageApp.class.getResource("/Images/AHAHAHAHA.png")));
        JLabel label = new JLabel(icon);

        label.setBounds(0, 0, 1284, 982);
        frame.add(label);
        frame.getContentPane().add(label);
        frame.setVisible(true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                frame.dispose();
                System.exit(0);
            }
        }, 4000L);
    }
}
