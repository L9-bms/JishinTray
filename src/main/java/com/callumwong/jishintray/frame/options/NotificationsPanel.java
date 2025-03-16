package com.callumwong.jishintray.frame.options;

import com.callumwong.jishintray.config.AppConfig;
import com.callumwong.jishintray.util.StringUtil;
import com.callumwong.jishintray.util.ThemeUtil;
import org.apache.commons.configuration2.Configuration;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class NotificationsPanel extends JPanel {
    NotificationsPanel() {
        Configuration config = AppConfig.getInstance().getConfig();

        add(new JLabel(StringUtil.getLocalizedString("setting.appearance.theme")));
        ButtonGroup minimumIntensity = new ButtonGroup();
        int[] intensities = new int[]{10, 20, 30, 40, 50, 55, 60, 65, 70};
        for (int intensity : intensities) {
            JRadioButton intensityButton = new JRadioButton(
                    StringUtil.scaleToString(intensity),
                    config.getInt("minimum_intensity") == intensity
            );
            intensityButton.addActionListener(e -> config.setProperty("minimum_intensity", intensity));

            minimumIntensity.add(intensityButton);
            this.add(intensityButton);
        }
    }
}
