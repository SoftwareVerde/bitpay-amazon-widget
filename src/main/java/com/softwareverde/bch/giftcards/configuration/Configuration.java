package com.softwareverde.bch.giftcards.configuration;

import com.softwareverde.bitcoin.inflater.MasterInflater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
    protected final ServerProperties _serverProperties;

    public Configuration(final File configurationFile, final MasterInflater masterInflater) {
        final Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(configurationFile));
        }
        catch (final IOException exception) { }

        final ServerPropertiesLoader serverPropertiesLoader = new ServerPropertiesLoader(masterInflater);
        _serverProperties = serverPropertiesLoader.loadServerProperties(properties);
    }

    public ServerProperties getServerProperties() { return _serverProperties; }
}
