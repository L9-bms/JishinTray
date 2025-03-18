package com.callumwong.jishintray.config;

import com.callumwong.jishintray.util.StringUtil;

public enum AlertType {
    SCALE_AND_DESTINATION,
    DESTINATION,
    SCALE_PROMPT,
    DETAIL_SCALE,
    FOREIGN,
    TSUNAMI,
    EEW_ALERT;

    public String getName() {
        return StringUtil.getLocalizedString("setting.notifications.types." + toString().toLowerCase());
    }
}
