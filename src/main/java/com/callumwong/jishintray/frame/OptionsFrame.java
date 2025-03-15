package com.callumwong.jishintray.frame;

import com.callumwong.jishintray.config.AppConfig;
import com.callumwong.jishintray.util.StringUtil;
import com.callumwong.jishintray.util.ThemeUtil;
import org.apache.commons.configuration2.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;

public class OptionsFrame extends DialogFrame {
    public OptionsFrame(boolean visible) {
        super(visible, "Options");
    }

    @Override
    protected void createUI() {
        Configuration config = AppConfig.getInstance().getConfig();

        add(new JLabel(StringUtil.getLocalizedString("setting.theme")));
        JComboBox<String> settingsComboBox = new JComboBox<>(ThemeUtil.Theme.names());
        settingsComboBox.setSelectedItem(config.getString("theme").toLowerCase());
        settingsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                config.setProperty("theme", ThemeUtil.setTheme(e.getItem().toString()));
            }
        });
        add(settingsComboBox, "wrap");

        add(new JLabel(StringUtil.getLocalizedString("setting.opacity")));
        JSlider opacitySlider = new JSlider(0, 100, config.getInt("opacity"));
        opacitySlider.setMajorTickSpacing(20);
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setSnapToTicks(true);
        opacitySlider.addChangeListener(e -> config.setProperty("opacity", opacitySlider.getValue()));
        add(opacitySlider, "wrap");

        add(new JLabel(StringUtil.getLocalizedString("setting.fade_out")));
        JCheckBox fadeOutCheckBox = new JCheckBox("", config.getBoolean("fade_out"));
        JSlider fadeOutDelaySlider = new JSlider(0, 30, config.getInt("fade_out_delay"));

        fadeOutCheckBox.addChangeListener(e -> {
            config.setProperty("fade_out", fadeOutCheckBox.isSelected());
            fadeOutDelaySlider.setEnabled(fadeOutCheckBox.isSelected());
        });
        add(fadeOutCheckBox, "wrap");

        add(new JLabel(StringUtil.getLocalizedString("setting.fade_out_delay")));
        fadeOutDelaySlider.setMajorTickSpacing(10);
        fadeOutDelaySlider.setMinorTickSpacing(5);
        fadeOutDelaySlider.setPaintTicks(true);
        fadeOutDelaySlider.setPaintLabels(true);
        fadeOutDelaySlider.addChangeListener(e -> config.setProperty("fade_out_delay", fadeOutDelaySlider.getValue()));
        fadeOutDelaySlider.setEnabled(fadeOutCheckBox.isSelected());
        add(fadeOutDelaySlider, "wrap");

        addCloseButton();

        JButton openDataDirButton = new JButton(
                String.format("<html><a href=\\\"\\\">%s</a></html>", StringUtil.getLocalizedString("button.data_dir")));
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

        add(openDataDirButton, "span 2, center, gaptop 10");
    }
}
