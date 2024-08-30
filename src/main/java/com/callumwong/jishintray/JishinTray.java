package com.callumwong.jishintray;

import com.formdev.flatlaf.FlatDarkLaf;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.URI;
import java.net.URL;

public class JishinTray {
    private static final Logger logger = LoggerFactory.getLogger(JishinTray.class);

    public static void main(String[] args) {
        logger.info("Starting JishinTray");

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        ConfigurationFrame configurationFrame = new ConfigurationFrame(false);

        SystemTray.FORCE_TRAY_TYPE = SystemTray.TrayType.Swing;
        SystemTray tray = SystemTray.get();
        if (tray == null) {
            throw new RuntimeException("Unable to load SystemTray!");
        }

        URL url = JishinTray.class.getClassLoader().getResource("icon.png");
        tray.setImage(url);
        tray.setStatus("test");
        tray.getMenu().add(new MenuItem("Open", e -> {
            configurationFrame.setVisible(true);
            configurationFrame.toFront();
            configurationFrame.requestFocus();
        }));

        P2PQuakeClient c = new P2PQuakeClient(URI.create("wss://api-realtime-sandbox.p2pquake.net/v2/ws"));
        c.connect();
    }
}
