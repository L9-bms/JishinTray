package com.callumwong.jishintray.frame;

import javax.swing.*;

public class AboutFrame extends DialogFrame {
    public AboutFrame(boolean visible) {
        super(visible);
    }

    @Override
    protected void createUI() {
        add(new JLabel("<html><b>JishinTray by callumwong.com</b></html>"));
        // TODO
    }
}
