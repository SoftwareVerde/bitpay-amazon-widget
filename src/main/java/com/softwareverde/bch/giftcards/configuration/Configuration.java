package com.softwareverde.bch.giftcards.configuration;

import com.softwareverde.bitcoin.inflater.MasterInflater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
    protected final DatabaseProperties _databaseProperties;
    protected final ServerProperties _serverProperties;
    protected final BitcoinProperties _bitcoinProperties;

    public Configuration(final File configurationFile, final MasterInflater masterInflater) {
        final Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(configurationFile));
        }
        catch (final IOException exception) { }

        final DatabasePropertiesLoader databasePropertiesLoader = new DatabasePropertiesLoader();
        _databaseProperties = databasePropertiesLoader.loadDatabaseProperties(properties);

        final ServerPropertiesLoader serverPropertiesLoader = new ServerPropertiesLoader(masterInflater);
        _serverProperties = serverPropertiesLoader.loadServerProperties(properties);

        final BitcoinPropertiesLoader bitcoinPropertiesLoader = new BitcoinPropertiesLoader();
        _bitcoinProperties = bitcoinPropertiesLoader.loadBitcoinProperties(properties);
    }

    public DatabaseProperties getDatabaseProperties() { return _databaseProperties; }
    public ServerProperties getServerProperties() { return _serverProperties; }
    public BitcoinProperties getBitcoinProperties() { return _bitcoinProperties; }
}
