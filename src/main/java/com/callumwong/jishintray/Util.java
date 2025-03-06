package com.callumwong.jishintray;

import java.math.BigDecimal;

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
}
