package com.callumwong.jishintray.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import dorkbox.os.OS;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Arrays;

public class ThemeUtil {
    private static final Logger logger = LoggerFactory.getLogger(ThemeUtil.class);

    public enum Theme {
        // TODO: Add more FlatLaf themes
        DARK,
        LIGHT;

        public static String[] names() {
            return Arrays.stream(values()).map(theme ->
                    WordUtils.capitalizeFully(theme.name())).toArray(String[]::new);
        }
    }

    public static String setTheme(String name) {
        return setTheme(Arrays.stream(Theme.values())
                .filter(theme -> name.equalsIgnoreCase(theme.toString())).findAny()
                .orElseGet(() -> {
                    logger.warn("unsupported theme '{}', resetting to dark", name);
                    return setTheme(Theme.DARK);
                })).toString().toLowerCase();
    }

    public static Theme setTheme(Theme theme) {
        boolean mac = OS.INSTANCE.isMacOsX();

        try {
            switch (theme) {
                case DARK -> UIManager.setLookAndFeel(mac ? new FlatMacDarkLaf() : new FlatDarkLaf());
                case LIGHT -> UIManager.setLookAndFeel(mac ? new FlatMacLightLaf() : new FlatLightLaf());
            }
        } catch (UnsupportedLookAndFeelException ex) {
            logger.warn("unsupported look and feel", ex);
        }

        FlatLaf.updateUILater();
        return theme;
    }
}
