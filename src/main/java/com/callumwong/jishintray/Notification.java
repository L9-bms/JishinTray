package com.callumwong.jishintray;

import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class Notification extends JFrame {
    private final String title;
    private final String description;
    private final Map<String, String> fields;
    private final URL image;

    private float initialOpacity;

    public Notification(String title, String description, Map<String, String> fields, URL image) {
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

        initialOpacity = (float) AppConfig.getConfig().getInt("opacity", 80) / 100f;
        Timer delayTimer = fadeOpacityTimer();
        delayTimer.start();
    }

    private @NotNull Timer fadeOpacityTimer() {
        Timer delayTimer = new Timer(10000, e -> {
            // Start the fade-in after 10 seconds
            ((Timer) e.getSource()).stop(); // Stop the delay timer

            // Timer to gradually fade out the JFrame
            Timer fadeInTimer = new Timer(50, new ActionListener() {
                float opacity = initialOpacity;

                @Override
                public void actionPerformed(ActionEvent e) {
                    // Check if mouse hovering, if so then set opacity to 1
                    Point mousePos = MouseInfo.getPointerInfo().getLocation();
                    Rectangle bounds = getBounds();
                    bounds.setLocation(getLocationOnScreen());
                    if (bounds.contains(mousePos)) {
                        opacity = 1f;
                    }

                    opacity -= 0.02f; // Decrement the opacity
                    if (opacity <= 0f) {
                        opacity = 0f;
                        ((Timer) e.getSource()).stop(); // Stop the timer once no longer visible
                        dispose(); // Dispose of the frame
                    }
                    setOpacity(opacity);
                }
            });

            fadeInTimer.start();
        });

        delayTimer.setRepeats(false); // Set the delay timer to run only once
        return delayTimer;
    }

    private void createUI() {
        setLayout(new MigLayout("insets 10 10 10 10", "[]", "[]"));

        Border blackline = BorderFactory.createTitledBorder(title);
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("insets 10 10 10 10", "[]10[]", "[]"));

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font(descriptionLabel.getFont().getFontName(), Font.BOLD, 14));
        panel.add(descriptionLabel, "wrap, span");

        fields.forEach((key, value) -> {
            JLabel nameLabel = new JLabel(key);
            JLabel valueLabel = new JLabel(value);
            panel.add(nameLabel);
            panel.add(valueLabel, "wrap");
        });

        try {
            BufferedImage image = ImageIO.read(this.image);

            int desiredWidth = 320;
            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();
            int scaledHeight = (int) (((double) originalHeight / originalWidth) * desiredWidth);

            ImageIcon imageIcon = new ImageIcon(image.getScaledInstance(desiredWidth, scaledHeight, Image.SCALE_FAST));
            JLabel imageLabel = new JLabel(imageIcon);

            panel.add(imageLabel, "align center, span, wrap");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JButton closeButton = new JButton("Dismiss");
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton, "align center, span");

        panel.setBorder(blackline);
        add(panel, BorderLayout.CENTER);
    }
}
