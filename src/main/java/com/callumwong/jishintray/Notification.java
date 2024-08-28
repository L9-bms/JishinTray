package com.callumwong.jishintray;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class Notification extends JFrame {
    public Notification() {
        // Create window
        super();

        setUndecorated(true);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 10 10 10 10", "[]", "[]"));

        // Add contents of window
        JLabel messageLabel = new JLabel("This is a notification popup!");
        add(messageLabel, "wrap");

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        add(closeButton, "align center");

        pack();

        // Set window size
        Rectangle usableBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int x = usableBounds.width - getWidth();
        int y = usableBounds.height - getHeight();
        setLocation(x, y);

        setVisible(true);

        setOpacity(0.5f);
    }
}
