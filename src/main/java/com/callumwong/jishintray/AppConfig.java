package com.callumwong.jishintray;

import net.harawata.appdirs.AppDirsFactory;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppConfig {
    private static final String CONFIGURATION_PATH = AppDirsFactory.getInstance().getUserConfigDir(
            JishinTray.APP_NAME,
            JishinTray.APP_VERSION,
            JishinTray.APP_AUTHOR
    );

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final Configurations configs = new Configurations();

    public static FileBasedConfigurationBuilder<PropertiesConfiguration> configBuilder;

    public static void loadConfig() {
        Path dir = Paths.get(CONFIGURATION_PATH);

        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File file = new File(CONFIGURATION_PATH, "config.properties");
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        configBuilder = configs.propertiesBuilder(file);
    }

    public static Configuration getConfig() {
        try {
            return configBuilder.getConfiguration();
        } catch (ConfigurationException e) {
            logger.error("failed to get configuration", e);
        }

        return null;
    }

    public static void saveConfig() {
        try {
            configBuilder.save();
        } catch (ConfigurationException e) {
            logger.error("failed to save configuration", e);
        }
    }
}
