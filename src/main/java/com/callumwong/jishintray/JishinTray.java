package com.callumwong.jishintray;

import com.callumwong.jishintray.config.AppConfig;
import com.callumwong.jishintray.frame.AboutFrame;
import com.callumwong.jishintray.frame.WelcomeFrame;
import com.callumwong.jishintray.frame.options.OptionsFrame;
import com.callumwong.jishintray.util.ThemeUtil;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class JishinTray {
    public static final String APP_NAME = "JishinTray";
    public static final String APP_AUTHOR = "callumwong.com";
    public static final String APP_VERSION = ObjectUtils.defaultIfNull(
            JishinTray.class.getPackage().getImplementationVersion(), "DEVELOPMENT");
    private static final Marker fatal = MarkerFactory.getMarker("FATAL");
    private static final Logger log = LoggerFactory.getLogger(JishinTray.class);

    private static Locale currentLocale;
    private static ResourceBundle messages;

    public JishinTray() {

        System.setProperty("apple.awt.enableTemplateImages", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        AppConfig.getInstance().getConfig().setProperty("theme",
                ThemeUtil.setTheme(AppConfig.getInstance().getConfig().getString("theme")));

        initTray();

        String webSocketUrl = ObjectUtils.defaultIfNull(
                System.getenv("JISHINTRAY_WEBSOCKET_URL"), "wss://api.p2pquake.net/v2/ws");

        if (AppConfig.getInstance().isFirstRun()) {
            new WelcomeFrame();
        }

        try {
            P2PQuakeClient wsClient = new P2PQuakeClient(new URI(webSocketUrl));
            wsClient.connect();

            Runtime.getRuntime().addShutdownHook(new Thread(wsClient::close));
        } catch (URISyntaxException e) {
            log.error(fatal, messages.getString("error.websocket.url"));
            System.exit(1);
        }
    }

    public static ResourceBundle getMessages() {
        return messages;
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static void main(String[] args) {
        currentLocale = Locale.getDefault();
        messages = ResourceBundle.getBundle("i18n.messages", currentLocale);

        new JishinTray();
    }

    private void initTray() {
        AboutFrame aboutFrame = new AboutFrame(false);

        SystemTray.FORCE_TRAY_TYPE = SystemTray.TrayType.Swing;
        SystemTray tray = SystemTray.get();
        if (tray == null) {
            log.error(fatal, messages.getString("error.tray"));
            System.exit(1);
        }

        URL url = JishinTray.class.getClassLoader().getResource("icon.png");
        tray.setImage(Objects.requireNonNull(url));

        tray.setStatus(messages.getString("tray.status.init"));
        tray.getMenu().add(new MenuItem(messages.getString("tray.menu.config"), new ActionListener() {
            OptionsFrame optionsFrame;

            @Override
            public void actionPerformed(ActionEvent e) {
                optionsFrame = new OptionsFrame(true);
            }
        }));
        tray.getMenu().add(new MenuItem(messages.getString("tray.menu.about"), e -> aboutFrame.setVisible(true)));
        tray.getMenu().add(new JSeparator());
        tray.getMenu().add(new MenuItem(messages.getString("tray.menu.exit"), e -> System.exit(0)));
    }
}
