package com.callumwong.jishintray;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ConfigurationFrame extends JFrame {
    public ConfigurationFrame(boolean visible) {
        super();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 20 20 20 20", "[]", "[]"));

        add(new JLabel("Start at login"));
        JCheckBox startAtLoginCheckbox = new JCheckBox();
        add(startAtLoginCheckbox, "wrap");

        // Add a slider
        add(new JLabel("Opacity"));
        JSlider opacitySlider = new JSlider(0, 100, 80);
        opacitySlider.setMajorTickSpacing(25);
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        add(opacitySlider, "wrap");

        add(new JLabel("Theme"));
        JComboBox<String> settingsComboBox = new JComboBox<>(new String[]{"Dark", "Light", "System"});
        add(settingsComboBox, "wrap");

        JButton applyButton = new JButton("Apply");
        JButton cancelButton = new JButton("Close");

        cancelButton.addActionListener(e -> dispose());

        add(applyButton, "span, split 2, center");
        add(cancelButton);

        pack();

        setTitle("Options");
        setLocationRelativeTo(null);
        setVisible(visible);
    }
}
