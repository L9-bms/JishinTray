package com.callumwong.jishintray.frame.options;

import com.callumwong.jishintray.config.AppConfig;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration2.Configuration;

import javax.swing.*;

public abstract class OptionsPanel extends JPanel {
    protected final Configuration config = AppConfig.getInstance().getConfig();

    protected OptionsPanel() {
        setLayout(new MigLayout("debug", "[]", "[]"));
    }
}
