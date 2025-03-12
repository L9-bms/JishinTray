package com.callumwong.jishintray.config;

import com.callumwong.jishintray.JishinTray;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.ReloadingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    private final ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builder;

    private static AppConfig INSTANCE;

    public AppConfig() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        String appDir = appDirs.getUserDataDir(JishinTray.APP_NAME, null, JishinTray.APP_AUTHOR, true);

        Path dir = Paths.get(appDir);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File file = new File(appDir, "config.properties");
        if (!file.exists()) {
            try {
                InputStream defaultConfig = JishinTray.class.getClassLoader().getResourceAsStream("config.properties");
                // TODO: add comments
                Files.copy(Objects.requireNonNull(defaultConfig), file.toPath());
            } catch (IOException e) {
                throw new RuntimeException("failed to copy default config to app dir", e);
            }
        }

        Parameters params = new Parameters();

        builder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.fileBased().setFile(file));
        builder.setAutoSave(true);
        builder.addEventListener(ConfigurationBuilderEvent.CONFIGURATION_REQUEST, event ->
                builder.getReloadingController().checkForReloading(null));
        builder.getReloadingController().addEventListener(ReloadingEvent.ANY, event ->
                logger.info("(re)loading configuration"));
    }

    public Configuration getConfig() {
        try {
            return builder.getConfiguration();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> getConfigBuilder() {
        return builder;
    }

    public static AppConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppConfig();
        }

        return INSTANCE;
    }
}
