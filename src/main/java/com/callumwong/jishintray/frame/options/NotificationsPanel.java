package com.callumwong.jishintray.frame.options;

import com.callumwong.jishintray.config.AlertType;
import com.callumwong.jishintray.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class NotificationsPanel extends OptionsPanel {
    NotificationsPanel() {
        add(new JLabel(StringUtil.getLocalizedString("setting.notifications.minimum_intensity")));
        ButtonGroup minimumIntensity = new ButtonGroup();
        int[] intensities = new int[]{10, 20, 30, 40, 45, 50, 55, 60, 70};
        for (int i = 0; i < intensities.length; i++) {
            int intensity = intensities[i];
            JRadioButton intensityButton = new JRadioButton(
                    StringUtil.scaleToString(intensity),
                    config().getInt("minimum_intensity") == intensity
            );
            intensityButton.addActionListener(e -> config().setProperty("minimum_intensity", intensity));

            minimumIntensity.add(intensityButton);
            this.add(intensityButton, "cell %s %s%s".formatted((i % 4) + 1, i / 4, i == intensities.length - 1 ? ", wrap" : ""));
        }

        add(new JLabel(StringUtil.getLocalizedString("setting.notifications.types")));

        Set<String> subscribedEventNames = Arrays.stream(config().getString("subscribed_events").toUpperCase().split(","))
                .collect(Collectors.toSet());

        Set<AlertType> selectedAlertTypes = Arrays.stream(AlertType.values())
                .filter(alertType -> subscribedEventNames.contains(alertType.toString()))
                .collect(Collectors.toSet());

        int i = 3;
        for (AlertType type : AlertType.values()) {
            JCheckBox typeCheckBox = new JCheckBox(type.getName(), selectedAlertTypes.contains(type));
            typeCheckBox.addActionListener(e -> {
                if (typeCheckBox.isSelected()) selectedAlertTypes.add(type);
                else selectedAlertTypes.remove(type);

                config().setProperty("subscribed_events", StringUtils.join(selectedAlertTypes, ","));
            });

            this.add(typeCheckBox, "cell 1 %s 4 1".formatted(i++));
        }
    }
}
