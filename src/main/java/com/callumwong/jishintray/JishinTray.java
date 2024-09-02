package com.callumwong.jishintray;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.URI;
import java.net.URL;

public class JishinTray {
    public static final String APP_NAME = "JishinTray";
    public static final String APP_AUTHOR = "65-7a";
    public static final String APP_VERSION = JishinTray.class.getPackage().getImplementationVersion();

    private static final Logger logger = LoggerFactory.getLogger(JishinTray.class);

    public static void main(String[] args) {
        logger.info("Starting JishinTray");

        AppConfig.loadConfig();
        if (AppConfig.getConfig() == null) throw new RuntimeException();

        System.setProperty("apple.awt.enableTemplateImages", "true");

        try {
            UIManager.setLookAndFeel(AppConfig.getConfig().getString("theme", "Dark").equals("Dark") ? new FlatDarkLaf() : new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        OptionsFrame configurationFrame = new OptionsFrame(false);

        SystemTray.FORCE_TRAY_TYPE = SystemTray.TrayType.Swing;
        SystemTray tray = SystemTray.get();
        if (tray == null) {
            throw new RuntimeException("Unable to load SystemTray!");
        }

        URL url = JishinTray.class.getClassLoader().getResource("icon.png");
        tray.setImage(url);

        tray.setStatus("Connected to P2PQuake");
        tray.getMenu().add(new MenuItem("Options", e -> {
            configurationFrame.setVisible(true);
            configurationFrame.toFront();
            configurationFrame.requestFocus();
        }));
        tray.getMenu().add(new MenuItem("About", e -> {

        }));

//        P2PQuakeClient c = new P2PQuakeClient(URI.create("wss://api-realtime-sandbox.p2pquake.net/v2/ws"));
        P2PQuakeClient c = new P2PQuakeClient(URI.create("wss://api.p2pquake.net/v2/ws"));
        c.connect();
    }
}
