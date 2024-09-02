package com.callumwong.jishintray;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration2.Configuration;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class OptionsFrame extends JFrame {
    public OptionsFrame(boolean visible) {
        super();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 20 20 20 20", "[]", "[]"));

        Configuration configuration = AppConfig.getConfig();
        if (configuration == null) {
            throw new RuntimeException();
        }

        add(new JLabel("Theme"));
        JComboBox<String> settingsComboBox = new JComboBox<>(new String[]{"Dark", "Light"});
        settingsComboBox.setSelectedItem(configuration.getString("theme", "Dark"));
        settingsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                configuration.setProperty("theme", e.getItem());
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
        JSlider volumeSlider = new JSlider(0, 100, configuration.getInt("volume", 100));
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setSnapToTicks(true);
        volumeSlider.addChangeListener(e -> {
            configuration.setProperty("volume", volumeSlider.getValue());
        });
        add(volumeSlider, "wrap");

        add(new JLabel("Opacity"));
        JSlider opacitySlider = new JSlider(0, 100, configuration.getInt("opacity", 80));
        opacitySlider.setMajorTickSpacing(25);
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setSnapToTicks(true);
        opacitySlider.addChangeListener(e -> {
            configuration.setProperty("opacity", opacitySlider.getValue());
        });
        add(opacitySlider, "wrap");

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Close");

        saveButton.addActionListener(e -> AppConfig.saveConfig());
        cancelButton.addActionListener(e -> dispose());

        add(saveButton, "span, split 2, center");
        add(cancelButton);

        pack();

        setTitle("Options");
        setLocationRelativeTo(null);
        setVisible(visible);
    }
}
