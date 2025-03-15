package com.callumwong.jishintray.frame;

import com.callumwong.jishintray.util.StringUtil;

import javax.swing.*;

public class WelcomeFrame extends DialogFrame {
    public WelcomeFrame() {
        super(true, "Welcome");
    }

    @Override
    protected void createUI() {
        add(new JLabel(String.format("<html>%s</html>", StringUtil.getLocalizedString("string.welcome"))), "wrap");

        addCloseButton();
    }
}
