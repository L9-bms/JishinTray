package com.callumwong.jishintray.frame;

import com.callumwong.jishintray.JishinTray;
import com.callumwong.jishintray.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

public class AboutFrame extends DialogFrame {
    private static final Logger log = LoggerFactory.getLogger(AboutFrame.class);

    public AboutFrame(boolean visible) {
        super(visible, "About");
    }

    @Override
    protected void createUI() {
        add(new JLabel(String.format("""
                        <html>
                        <p><b>%s %s</b></p>
                        <p>Copyright (C) 2025 %s</p>
                        <p>%s</p>
                        <html>
                        """,
                JishinTray.APP_NAME,
                JishinTray.APP_VERSION,
                JishinTray.APP_AUTHOR,
                StringUtil.getLocalizedString("string.description"))
        ), "wrap");

        addLink(StringUtil.getLocalizedString("string.p2pquake"), "https://www.p2pquake.net/");
        addLink(StringUtil.getLocalizedString("string.open_source"), "https://github.com/65-7a/JishinTray");

        addCloseButton();
    }

    private void addLink(String message, String link) {
        JLabel label = new JLabel(String.format("<html><a href=\"\">%s</a><html>", message));
        label.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(URI.create(link));
                } catch (IOException ex) {
                    log.error("Failed to open hyperlink: {}", ex.getMessage());
                }
            }
        });

        add(label, "wrap");
    }
}
