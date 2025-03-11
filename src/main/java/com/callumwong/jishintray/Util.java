package com.callumwong.jishintray;

import com.callumwong.jishintray.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
    public static String scaleToString(BigDecimal scale) {
        return scaleToString(scale.intValue());
    }

    public static String scaleToString(int scale) {
        return switch (scale) {
            case 10 -> "1";
            case 20 -> "2";
            case 30 -> "3";
            case 40 -> "4";
            case 45 -> "5-";
            case 50 -> "5+";
            case 55 -> "6-";
            case 60 -> "6+";
            case 70 -> "7";
            default -> "N/A";
        };
    }

    public static String gradeToString(JMATsunamiAllOfAreas.GradeEnum grade) {
        return switch (grade) {
            case WATCH -> "Tsunami Advisory";
            case WARNING -> "Tsunami Warning";
            case MAJOR_WARNING -> "Major Tsunami Warning";
            default -> "N/A";
        };
    }

    public static String maxHeightToString(JMATsunamiAllOfMaxHeight.DescriptionEnum description) {
        return switch (description) {
            case u -> "Huge";
            case u2 -> "High";
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
            case u -> "Immediately";
            case u2 -> "On the way";
            case u3 -> "Arrived";
        };
    }
}
