package com.callumwong.jishintray;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

public class JishinTray {
    public static final String APP_NAME = "JishinTray";
    public static final String APP_AUTHOR = "callumwong.com";
    public static final String APP_VERSION = JishinTray.class.getPackage().getImplementationVersion();

    private static final Logger logger = LoggerFactory.getLogger(JishinTray.class);

    public JishinTray() {
        Configuration config = AppConfig.getInstance().getConfig();

        System.setProperty("apple.awt.enableTemplateImages", "true");
        SystemTray.FORCE_TRAY_TYPE = SystemTray.TrayType.Swing;

        try {
            UIManager.setLookAndFeel(config.getString("theme", "Dark").equals("Dark") ? new FlatDarkLaf() : new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        initTray();

//        P2PQuakeClient wsClient = new P2PQuakeClient(URI.create("wss://api-realtime-sandbox.p2pquake.net/v2/ws"));
        P2PQuakeClient wsClient = new P2PQuakeClient(URI.create("wss://api.p2pquake.net/v2/ws"));
        wsClient.connect();

        Runtime.getRuntime().addShutdownHook(new Thread(wsClient::close));
    }

    private void initTray() {
        OptionsFrame configurationFrame = new OptionsFrame(false);

        SystemTray tray = SystemTray.get();
        if (tray == null) {
            throw new RuntimeException("Unable to load SystemTray!");
        }

        URL url = JishinTray.class.getClassLoader().getResource("icon.png");
        tray.setImage(Objects.requireNonNull(url));

        tray.setStatus("Connected to P2PQuake");
        tray.getMenu().add(new MenuItem("Options", e -> {
            configurationFrame.setVisible(true);
            configurationFrame.toFront();
            configurationFrame.requestFocus();
        }));
        tray.getMenu().add(new MenuItem("About", e -> {

        }));
    }

    public static void main(String[] args) {
        logger.info("Starting JishinTray");

        new JishinTray();
    }
}
