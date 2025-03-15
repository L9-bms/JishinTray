package com.callumwong.jishintray.util;

import com.callumwong.jishintray.JishinTray;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilTest {
    @Test
    void issueTimeToLocalizedString() {
        String time = "2025/03/12 23:22:53";

        try (MockedStatic<JishinTray> jishinTrayMockedStatic = Mockito.mockStatic(JishinTray.class)) {
            jishinTrayMockedStatic.when(JishinTray::getCurrentLocale).thenReturn(Locale.of("en", "AU"));
                assertEquals("13 March 2025, 1:22:53 am AEDT", StringUtil.issueTimeToLocalizedString(time));
        }
    }
}