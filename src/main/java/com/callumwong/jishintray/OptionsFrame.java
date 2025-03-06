package com.callumwong.jishintray;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration2.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;

public class OptionsFrame extends JFrame {
    private final Configuration config;

    public OptionsFrame(boolean visible) {
        super();

        config = AppConfig.getInstance().getConfig();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 20 20 20 20", "[]", "[]"));

        createUI();
        pack();

        setTitle("Options");
        setLocationRelativeTo(null);
        setVisible(visible);
    }

    private void createUI() {
        add(new JLabel("Theme"));
        JComboBox<String> settingsComboBox = new JComboBox<>(new String[]{"Dark", "Light"});
        settingsComboBox.setSelectedItem(config.getString("theme", "Dark"));
        settingsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                config.setProperty("theme", e.getItem());
                try {
                    UIManager.setLookAndFeel(e.getItem() == "Dark" ? new FlatDarkLaf() : new FlatLightLaf());
                } catch (UnsupportedLookAndFeelException ex) {
                    throw new RuntimeException(ex);
                }
                FlatLaf.updateUILater();
            }
        });
        add(settingsComboBox, "wrap");

        add(new JLabel("Volume"));
        JSlider volumeSlider = new JSlider(0, 100, config.getInt("volume", 100));
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setSnapToTicks(true);
        volumeSlider.addChangeListener(e -> config.setProperty("volume", volumeSlider.getValue()));
        add(volumeSlider, "wrap");

        add(new JLabel("Opacity"));
        JSlider opacitySlider = new JSlider(0, 100, config.getInt("opacity", 75));
        opacitySlider.setMajorTickSpacing(25);
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setSnapToTicks(true);
        opacitySlider.addChangeListener(e -> config.setProperty("opacity", opacitySlider.getValue()));
        add(opacitySlider, "wrap");

        add(new JLabel("Fade out"));
        JCheckBox fadeOutCheckBox = new JCheckBox("", config.getBoolean("fade_out", true));
        JSlider fadeOutDelaySlider = new JSlider(0, 30, config.getInt("fade_out_delay", 10));

        fadeOutCheckBox.addChangeListener(e -> {
            config.setProperty("fade_out", fadeOutCheckBox.isSelected());
            fadeOutDelaySlider.setEnabled(fadeOutCheckBox.isSelected());
        });
        add(fadeOutCheckBox, "wrap");

        add(new JLabel("Fade out delay"));
        fadeOutDelaySlider.setMajorTickSpacing(10);
        fadeOutDelaySlider.setMinorTickSpacing(5);
        fadeOutDelaySlider.setPaintTicks(true);
        fadeOutDelaySlider.setPaintLabels(true);
        fadeOutDelaySlider.setSnapToTicks(true);
        fadeOutDelaySlider.addChangeListener(e -> config.setProperty("fade_out_delay", fadeOutDelaySlider.getValue()));
        fadeOutDelaySlider.setEnabled(fadeOutCheckBox.isSelected());
        add(fadeOutDelaySlider, "wrap");

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Close");

        saveButton.addActionListener(e -> AppConfig.getInstance().saveConfig());
        cancelButton.addActionListener(e -> dispose());

        add(saveButton, "span, split 2, center");
        add(cancelButton, "wrap");

        JButton openDataDirButton = new JButton("<html><a href=\\\"\\\">Open data directory</a></html>");
        openDataDirButton.setBorderPainted(false);
        openDataDirButton.setFocusPainted(false);
        openDataDirButton.setContentAreaFilled(false);

        openDataDirButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(AppConfig.getInstance().getConfigBuilder().getFileHandler().getFile().getParentFile());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        add(openDataDirButton, "span 2, center");
    }
}
