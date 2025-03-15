package com.callumwong.jishintray;

import com.callumwong.jishintray.config.AppConfig;
import com.callumwong.jishintray.frame.AboutFrame;
import com.callumwong.jishintray.frame.OptionsFrame;
import com.callumwong.jishintray.frame.WelcomeFrame;
import com.callumwong.jishintray.util.ThemeUtil;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public class JishinTray {
    public static final String APP_NAME = "JishinTray";
    public static final String APP_AUTHOR = "callumwong.com";
    public static final String APP_VERSION = ObjectUtils.defaultIfNull(
            JishinTray.class.getPackage().getImplementationVersion(), "DEVELOPMENT");

    private static final Logger log = LoggerFactory.getLogger(JishinTray.class);

    public JishinTray() {
        Configuration config = AppConfig.getInstance().getConfig();

        System.setProperty("apple.awt.enableTemplateImages", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        config.setProperty("theme", ThemeUtil.setTheme(config.getString("theme")));

        initTray();

        String webSocketUrl = System.getenv("JISHINTRAY_WEBSOCKET_URL");
        if (webSocketUrl == null) {
            webSocketUrl = "wss://api.p2pquake.net/v2/ws";
        }

        if (AppConfig.getInstance().isFirstRun()) {
            new WelcomeFrame();
        }

        try {
            P2PQuakeClient wsClient = new P2PQuakeClient(new URI(webSocketUrl));
            wsClient.connect();

            Runtime.getRuntime().addShutdownHook(new Thread(wsClient::close));
        } catch (URISyntaxException e) {
            log.error(MarkerFactory.getMarker("FATAL"), "Invalid WebSocket URL! Exiting");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        log.info("Starting JishinTray");

        new JishinTray();
    }

    private void initTray() {
        AboutFrame aboutFrame = new AboutFrame(false);

        SystemTray.FORCE_TRAY_TYPE = SystemTray.TrayType.Swing;
        SystemTray tray = SystemTray.get();
        if (tray == null) {
            throw new RuntimeException("SystemTray is null");
        }

        URL url = JishinTray.class.getClassLoader().getResource("icon.png");
        tray.setImage(Objects.requireNonNull(url));

        tray.setStatus("Initialising...");
        tray.getMenu().add(new MenuItem("Options", new ActionListener() {
            OptionsFrame optionsFrame;

            @Override
            public void actionPerformed(ActionEvent e) {
                optionsFrame = new OptionsFrame(true);
            }
        }));
        tray.getMenu().add(new MenuItem("About", e -> aboutFrame.setVisible(true)));
        tray.getMenu().add(new JSeparator());
        tray.getMenu().add(new MenuItem("Exit", e -> System.exit(0)));
    }
}
