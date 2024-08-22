package com.callumwong.jishintray;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class JishinTray {
    private static final Logger logger = LoggerFactory.getLogger(JishinTray.class);

    public JishinTray() {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new MigLayout("insets 10 10 10 10", "[]", "[]"));

        JLabel messageLabel = new JLabel("This is a notification popup!");
        frame.add(messageLabel, "wrap");

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> {
            frame.dispose();
        });
        frame.add(closeButton, "align center");

        frame.pack();

//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle usableBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int x = usableBounds.width - frame.getWidth() - 20;
        int y = usableBounds.height - frame.getHeight() - 20;
        frame.setLocation(x, y);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        logger.info("Starting JishinTray");
        SwingUtilities.invokeLater(JishinTray::new);
    }
}
