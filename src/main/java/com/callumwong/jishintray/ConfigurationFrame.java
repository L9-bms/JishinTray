package com.callumwong.jishintray;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ConfigurationFrame extends JFrame {
    public ConfigurationFrame(boolean visible) {
        super();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new MigLayout("insets 10 10 10 10", "[]", "[]"));

        JLabel messageLabel = new JLabel("configuration");
        add(messageLabel, "wrap");

        pack();
        setLocationRelativeTo(null);
        setVisible(visible);
    }
}
