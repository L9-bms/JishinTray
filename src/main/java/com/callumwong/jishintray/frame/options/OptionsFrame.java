package com.callumwong.jishintray.frame.options;

import com.callumwong.jishintray.config.AppConfig;
import com.callumwong.jishintray.frame.DialogFrame;
import com.callumwong.jishintray.util.StringUtil;
import com.callumwong.jishintray.util.ThemeUtil;
import org.apache.commons.configuration2.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;

public class OptionsFrame extends DialogFrame {
    public OptionsFrame(boolean visible) {
        super(visible, "Options");
    }

    @Override
    protected void createUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(StringUtil.getLocalizedString("setting.appearance"), new AppearancePanel());
        tabbedPane.addTab(StringUtil.getLocalizedString("setting.notifications"), new NotificationsPanel());

        add(tabbedPane);
        addButtons();
    }

    private void addButtons() {
        addCloseButton();

        JButton openDataDirButton = new JButton(
                String.format("<html><a href=\\\"\\\">%s</a></html>", StringUtil.getLocalizedString("button.data_dir")));
        openDataDirButton.setBorderPainted(false);
        openDataDirButton.setFocusPainted(false);
        openDataDirButton.setContentAreaFilled(false);

        openDataDirButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(AppConfig.getInstance().getConfigBuilder().getFileHandler().getFile().getParentFile());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        add(openDataDirButton, "span 2, center, gaptop 10");
    }
}
