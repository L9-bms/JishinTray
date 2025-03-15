package com.callumwong.jishintray.frame;

import com.callumwong.jishintray.JishinTray;
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
                <p><b>JishinTray %s</b></p>
                <p>Copyright (C) 2025 %s</p>
                <p>A tray app that notifies you of earthquakes in Japan.</p>
                <html>
                """, JishinTray.APP_VERSION, JishinTray.APP_AUTHOR)), "wrap");

        JLabel line3 = new JLabel("<html><a href=\"\">Earthquake data courtesy of P2PQuake</a><html>");
        line3.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(URI.create("https://www.p2pquake.net/"));
                } catch (IOException ex) {
                    log.error("unable to open hyperlink", ex);
                }
            }
        });
        add(line3, "wrap");
        JLabel line4 = new JLabel("<html><a href=\"\">JishinTray is open source</a><html>");
        line4.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(URI.create("https://git.callumwong.com/callum/JishinTray"));
                } catch (IOException ex) {
                    log.error("unable to open hyperlink", ex);
                }
            }
        });
        add(line4, "wrap");

        addCloseButton();
    }
}
