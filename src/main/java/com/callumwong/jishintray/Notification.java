package com.callumwong.jishintray;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration2.Configuration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(Notification.class);

    private final String title;
    private final String description;
    private final Map<String, String> fields;
    private final URL image;

    private final Configuration config;
    private final float initialOpacity;

    public Notification(String title, String description, Map<String, String> fields, URL image) {
        // Create window
        super();

        config = AppConfig.getInstance().getConfig();

        this.title = title;
        this.description = description;
        this.fields = fields;
        this.image = image;

        initialOpacity = (float) config.getInt("opacity", 75) / 100f;

        setUndecorated(true);
        setOpacity(initialOpacity);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setFocusableWindowState(false);
        createUI();
        pack();

        // https://stackoverflow.com/questions/14431467/how-do-i-determine-the-position-of-the-system-tray-on-the-screen/
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Rectangle bounds = gd.getDefaultConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());

        Rectangle safeBounds = new Rectangle(bounds);
        safeBounds.x += insets.left;
        safeBounds.y += insets.top;
        safeBounds.width -= (insets.left + insets.right);
        safeBounds.height -= (insets.top + insets.bottom);

        int x = safeBounds.width + safeBounds.x - getWidth();
        int y = safeBounds.height + safeBounds.y - getHeight();
        setLocation(x, y);

        setVisible(true);

        if (config.getBoolean("fade_out", true)) {
            Timer delayTimer = fadeOpacityTimer();
            delayTimer.start();
        }
    }

    private @NotNull Timer fadeOpacityTimer() {
        Timer delayTimer = new Timer(config.getInt("fade_out_delay", 10) * 1000, e -> {
            ((Timer) e.getSource()).stop(); // Stop the delay timer

            // Timer to gradually fade out the JFrame
            Timer fadeInTimer = new Timer(50, new ActionListener() {
                float opacity = initialOpacity;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!isShowing()) return;

                    // Check if mouse hovering, if so then set opacity to 1
                    Point mousePos = MouseInfo.getPointerInfo().getLocation();
                    Rectangle bounds = getBounds();
                    bounds.setLocation(getLocationOnScreen());
                    if (bounds.contains(mousePos)) {
                        opacity = initialOpacity;
                    }

                    opacity -= 0.02f * initialOpacity; // Decrement the opacity
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

        JLabel descriptionLabel = new JLabel("<html>" + description + "</html>");
        descriptionLabel.setFont(new Font(descriptionLabel.getFont().getFontName(), Font.BOLD, 14));
        panel.add(descriptionLabel, "wrap, span");

        if (fields != null){
            fields.forEach((key, value) -> {
                JLabel nameLabel = new JLabel("<html>" + key + "</html>");
                JLabel valueLabel = new JLabel("<html>" + value + "</html>");
                panel.add(nameLabel);
                panel.add(valueLabel, "wrap");
            });
        }

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
            logger.error("failed to add image: {}", e.getMessage());
        }

        JButton closeButton = new JButton("Dismiss");
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton, "align center, span");

        panel.setBorder(blackline);
        add(panel, BorderLayout.CENTER);
    }
}
