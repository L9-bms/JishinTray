package com.callumwong.jishintray.util;

import com.callumwong.jishintray.JishinTray;
import com.callumwong.jishintray.model.*;

import java.math.BigDecimal;

public class StringUtil {
    public static String scaleToString(BigDecimal scale) {
        return scaleToString(scale.intValue());
    }

    public static String scaleToString(int scale) {
        String key = "string.earthquake.scale." + scale;
        if (JishinTray.getMessages().containsKey(key)) {
            return getLocalizedString(key);
        } else {
            return "N/A";
        }
    }

    public static String gradeToString(JMATsunamiAllOfAreas.GradeEnum grade) {
        return switch (grade) {
            case WATCH -> getLocalizedString("string.tsunami.grade.watch");
            case WARNING -> getLocalizedString("string.tsunami.grade.warning");
            case MAJOR_WARNING -> getLocalizedString("string.tsunami.grade.major_warning");
            default -> "N/A";
        };
    }

    public static String maxHeightToString(JMATsunamiAllOfMaxHeight.DescriptionEnum description) {
        return switch (description) {
            case u -> getLocalizedString("string.tsunami.height.huge");
            case u2 -> getLocalizedString("string.tsunami.height.high");
            case u3 -> ">10m";
            case u4 -> "10m";
            case u5 -> "5m";
            case u6 -> "3m";
            case u7 -> "1m";
            case u8 -> "<0.2m";
        };
    }

    public static String conditionToString(JMATsunamiAllOfFirstHeight.ConditionEnum condition) {
        return switch (condition) {
            case u -> getLocalizedString("string.tsunami.condition.immediately");
            case u2 -> getLocalizedString("string.tsunami.condition.otw");
            case u3 -> getLocalizedString("string.tsunami.condition.arrived");
        };
    }

    public static String getLocalizedString(String string) {
        return JishinTray.getMessages().getString(string);
    }
}
