package com.softwareverde.bch.giftcards.configuration;

import com.softwareverde.database.mysql.embedded.properties.EmbeddedDatabaseProperties;
import com.softwareverde.database.properties.DatabaseCredentials;

public class DatabaseProperties implements EmbeddedDatabaseProperties {
    protected Integer _port;
    protected String _schema;
    protected String _hostname;
    protected DatabaseCredentials _rootDatabaseCredentials;
    protected DatabaseCredentials _databaseCredentials;
    protected String _dataDirectory;
    protected Boolean _embeddedDatabaseEnabled;

    @Override
    public String getRootPassword() {
        return _rootDatabaseCredentials.password;
    }

    @Override
    public String getHostname() {
        return _hostname;
    }

    @Override
    public String getUsername() {
        return _databaseCredentials.username;
    }

    @Override
    public String getPassword() {
        return _databaseCredentials.password;
    }

    @Override
    public String getSchema() {
        return _schema;
    }

    @Override
    public Integer getPort() {
        return _port;
    }

    @Override
    public DatabaseCredentials getRootCredentials() {
        return _databaseCredentials;
    }

    @Override
    public DatabaseCredentials getCredentials() {
        return _databaseCredentials;
    }

    public Boolean useEmbeddedDatabase() {
        return _embeddedDatabaseEnabled;
    }

    @Override
    public String getDataDirectory() {
        return _dataDirectory;
    }
}
