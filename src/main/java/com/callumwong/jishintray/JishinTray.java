package com.callumwong.jishintray;

import com.callumwong.jishintray.config.AppConfig;
import com.callumwong.jishintray.frame.OptionsFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;
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
            logger.error("unsupported look and feel", e);
        }

        initTray();

        String webSocketUrl = System.getenv("JISHINTRAY_WEBSOCKET_URL");
        if (webSocketUrl == null) {
            webSocketUrl = "wss://api.p2pquake.net/v2/ws";
        }

        try {
            P2PQuakeClient wsClient = new P2PQuakeClient(new URI(webSocketUrl));
            wsClient.connect();

            Runtime.getRuntime().addShutdownHook(new Thread(wsClient::close));
        } catch (URISyntaxException e) {
            throw new RuntimeException("invalid websocket url", e);
        }
    }

    private void initTray() {
        OptionsFrame configurationFrame = new OptionsFrame(false);

        SystemTray tray = SystemTray.get();
        if (tray == null) {
            logger.error("Unable to load SystemTray! Continuing...");
            return;
        }

        URL url = JishinTray.class.getClassLoader().getResource("icon.png");
        tray.setImage(Objects.requireNonNull(url));

        tray.setStatus("Initialising...");
        tray.getMenu().add(new MenuItem("Options", e -> {
            configurationFrame.setVisible(true);
            configurationFrame.toFront();
            configurationFrame.requestFocus();
        }));
        tray.getMenu().add(new MenuItem("About", e -> {
            // TODO
        }));
        tray.getMenu().add(new JSeparator());
        tray.getMenu().add(new MenuItem("Exit", e -> System.exit(0)));
    }

    public static void main(String[] args) {
        logger.info("Starting JishinTray");

        new JishinTray();
    }
}
