package com.callumwong.jishintray.frame.options;

import com.callumwong.jishintray.util.StringUtil;

import javax.swing.*;

public class NotificationsPanel extends OptionsPanel {
    NotificationsPanel() {
        add(new JLabel(StringUtil.getLocalizedString("setting.notifications.minimum_intensity")));
        ButtonGroup minimumIntensity = new ButtonGroup();
        int[] intensities = new int[]{10, 20, 30, 40, 45, 50, 55, 60, 70};
        for (int i = 0; i < intensities.length; i++) {
            int intensity = intensities[i];
            JRadioButton intensityButton = new JRadioButton(
                    StringUtil.scaleToString(intensity),
                    config.getInt("minimum_intensity") == intensity
            );
            intensityButton.addActionListener(e -> config.setProperty("minimum_intensity", intensity));

            minimumIntensity.add(intensityButton);
            this.add(intensityButton, "cell %s %s%s".formatted((i % 2) + 1, i / 2, i == intensities.length - 1 ? ", wrap" : ""));
        }

        add(new JLabel(StringUtil.getLocalizedString("setting.notifications.types")));
        this.add(new JCheckBox("Scale Prompt", true), "span 2");
    }
}
