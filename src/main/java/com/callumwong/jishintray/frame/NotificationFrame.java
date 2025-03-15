package com.callumwong.jishintray.frame;

import com.callumwong.jishintray.config.AppConfig;
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
import java.util.HashMap;
import java.util.Map;

public class NotificationFrame extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(NotificationFrame.class);

    private final String title;
    private final String description;
    private final Map<String, JComponent> fields;
    private final URL image;

    private final Configuration config;
    private final float initialOpacity;

    public NotificationFrame(String title, String description, Map<String, JComponent> fields, URL image) {
        // Create window
        super();

        config = AppConfig.getInstance().getConfig();

        this.title = title;
        this.description = description;
        this.fields = fields;
        this.image = image;

        initialOpacity = (float) config.getInt("opacity") / 100f;

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
        Timer delayTimer = new Timer(config.getInt("fade_out_delay") * 1000, e -> {
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

        if (fields != null) {
            fields.forEach((key, value) -> {
                JLabel keyLabel = new JLabel("<html>" + key + "</html>");
                panel.add(keyLabel);
                panel.add(value, "wrap");
            });
        }

        if (image != null) {
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
                log.error("failed to add image: {}", e.getMessage());
            }
        }

        JButton closeButton = new JButton("Dismiss");
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton, "align center, span");

        panel.setBorder(blackline);
        add(panel, BorderLayout.CENTER);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private String title;
        private String description;
        private Map<String, JComponent> fields;
        private URL image;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setFields(Map<String, ?> fields) {
            Map<String, JComponent> map = new HashMap<>();
            fields.forEach((key, value) -> {
                if (value instanceof String) {
                    map.put(key, new JLabel("<html>" + value + "</html>"));
                } else if (value instanceof JComponent) {
                    map.put(key, (JComponent) value);
                } else {
                    throw new IllegalArgumentException("Unsupported field type: " + value.getClass());
                }
            });
            this.fields = map;
            return this;
        }

        public Builder setImage(URL image) {
            this.image = image;
            return this;
        }

        public NotificationFrame createNotification() {
            return new NotificationFrame(title, description, fields, image);
        }
    }
}
