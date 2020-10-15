package com.softwareverde.bch.giftcards.configuration;

import com.softwareverde.database.properties.DatabaseCredentials;
import com.softwareverde.util.Util;

import java.util.Properties;

public class DatabasePropertiesLoader {
    public DatabasePropertiesLoader() { }

    public DatabaseProperties loadDatabaseProperties(final Properties properties) {
        final DatabaseProperties databaseProperties = new DatabaseProperties();

        databaseProperties._hostname = properties.getProperty("database.url", "");
        databaseProperties._schema = properties.getProperty("database.schema", "");
        databaseProperties._port = Util.parseInt(properties.getProperty("database.port", ""));

        final String rootPassword = properties.getProperty("database.rootPassword", "");
        databaseProperties._rootDatabaseCredentials = new DatabaseCredentials("root", rootPassword);

        final String username = properties.getProperty("database.username", "");
        final String password = properties.getProperty("database.password", "");
        databaseProperties._databaseCredentials = new DatabaseCredentials(username, password);

        databaseProperties._embeddedDatabaseEnabled = Util.parseBool(properties.getProperty("database.useEmbeddedDatabase", "1"));
        databaseProperties._dataDirectory = properties.getProperty("database.dataDirectory", "data");

        return databaseProperties;
    }
}
