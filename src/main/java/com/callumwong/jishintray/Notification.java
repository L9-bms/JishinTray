package com.callumwong.jishintray;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.URI;
import java.util.Map;

public class Notification extends JFrame {
    private final String title;
    private final String description;
    private final Map<String, String> fields;
    private final URI image;

    public Notification(String title, String description, Map<String, String> fields, URI image) {
        // Create window
        super();

        this.title = title;
        this.description = description;
        this.fields = fields;
        this.image = image;

        setUndecorated(true);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        createUI();
        pack();

        // Set window size
        Rectangle usableBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int x = usableBounds.width - getWidth();
        int y = usableBounds.height - getHeight();
        setLocation(x, y);

        setVisible(true);

        setOpacity(0.5f);
    }

    private void createUI() {
        Border blackline = BorderFactory.createTitledBorder(title);
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("insets 10 10 10 10", "[]", "[]"));

        panel.add(new JLabel(description), "align center");

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton, "align center");

        panel.setBorder(blackline);
        add(panel, BorderLayout.CENTER);
    }
}
