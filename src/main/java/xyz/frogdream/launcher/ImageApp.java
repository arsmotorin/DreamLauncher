package xyz.frogdream.launcher;

import java.awt.LayoutManager;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ImageApp {
    public static void main() {
        final JFrame frame = new JFrame("AHAHAHAHAHAHHAHAHAHAHHAHAHAHA");
        frame.getContentPane().setLayout((LayoutManager)null);
        frame.setDefaultCloseOperation(3);
        frame.setSize(1284, 982);
        ImageIcon icon = new ImageIcon("/Images/AHAHAHAHA.png");
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
        }, 5000L);
    }
}
