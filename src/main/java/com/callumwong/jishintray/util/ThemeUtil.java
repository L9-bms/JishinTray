package com.callumwong.jishintray.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatNordIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import dorkbox.os.OS;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ThemeUtil {
    private static final Logger log = LoggerFactory.getLogger(ThemeUtil.class);

    public static String setTheme(String name) {
        return setTheme(Arrays.stream(Theme.values())
                .filter(theme -> name.replace(" ", "_").equalsIgnoreCase(theme.toString())).findAny()
                .orElseGet(() -> {
                    log.warn(StringUtil.getLocalizedString("error.unsupported_theme"), name);
                    return setTheme(Theme.DARK);
                })).toString();
    }

    public static Theme setTheme(Theme theme) {
        boolean mac = OS.INSTANCE.isMacOsX();

        switch (theme) {
            case DARK -> {
                if (mac) FlatMacDarkLaf.setup();
                else FlatDarkLaf.setup();
            }
            case LIGHT -> {
                if (mac) FlatLightLaf.setup();
                else FlatMacLightLaf.setup();
            }
            case ONE_DARK -> FlatOneDarkIJTheme.setup();
            case NORD -> FlatNordIJTheme.setup();
        }

        FlatLaf.updateUILater();
        return theme;
    }

    public enum Theme {
        DARK,
        LIGHT,
        ONE_DARK,
        NORD;

        public static String[] names() {
            return Arrays.stream(values()).map(theme ->
                    WordUtils.capitalizeFully(theme.name().replace("_", " "))).toArray(String[]::new);
        }
    }
}
