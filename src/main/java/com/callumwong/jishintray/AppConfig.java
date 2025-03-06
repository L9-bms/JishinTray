package com.callumwong.jishintray;

import net.harawata.appdirs.AppDirs;
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
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    private final FileBasedConfigurationBuilder<PropertiesConfiguration> builder;

    private static AppConfig INSTANCE;

    public AppConfig() throws ConfigurationException {
        AppDirs appDirs = AppDirsFactory.getInstance();
        String appDir = appDirs.getUserDataDir(JishinTray.APP_NAME, JishinTray.APP_VERSION, JishinTray.APP_AUTHOR, true);

        Path dir = Paths.get(appDir);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File file = new File(appDir, "config.properties");
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Unable to create new config file");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Configurations configs = new Configurations();
        builder = configs.propertiesBuilder(file);
    }

    public Configuration getConfig() {
        try {
            return builder.getConfiguration();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public FileBasedConfigurationBuilder<PropertiesConfiguration> getConfigBuilder() {
        return builder;
    }

    public void saveConfig() {
        try {
            builder.save();
            logger.info("saved config");
        } catch (ConfigurationException e) {
            logger.error("failed to save configuration", e);
        }
    }

    public static AppConfig getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new AppConfig();
            } catch (ConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

        return INSTANCE;
    }
}
