package com.callumwong.jishintray;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class JishinTray {
    private static final Logger logger = LoggerFactory.getLogger(JishinTray.class);

    public JishinTray() {
        JFrame frame = new JFrame();

    }

    public static void main(String[] args) {
        logger.info("Starting JishinTray");
//        SwingUtilities.invokeLater(JishinTray::new);

        P2PQuakeClient c = new P2PQuakeClient(URI.create("wss://api-realtime-sandbox.p2pquake.net/v2/ws"));
        c.connect();
    }
}
