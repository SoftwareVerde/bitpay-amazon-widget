package com.softwareverde.bch.giftcards.database;

import com.softwareverde.bch.giftcards.configuration.DatabaseProperties;
import com.softwareverde.bitcoin.server.database.Database;
import com.softwareverde.bitcoin.server.database.DatabaseConnection;
import com.softwareverde.bitcoin.server.database.DatabaseConnectionFactory;
import com.softwareverde.database.DatabaseException;
import com.softwareverde.database.DatabaseInitializer;
import com.softwareverde.database.mysql.MysqlDatabase;
import com.softwareverde.database.mysql.MysqlDatabaseConnection;
import com.softwareverde.database.mysql.MysqlDatabaseConnectionFactory;
import com.softwareverde.database.mysql.MysqlDatabaseInitializer;
import com.softwareverde.database.mysql.embedded.DatabaseCommandLineArguments;
import com.softwareverde.database.mysql.embedded.EmbeddedMysqlDatabase;
import com.softwareverde.database.properties.DatabaseCredentials;
import com.softwareverde.logging.Logger;

import java.sql.Connection;

public class BchGiftCardsDatabase implements Database {
    public static class InitFile {
        public final String sqlInitFile;
        public final Integer databaseVersion;

        public InitFile(final String sqlInitFile, final Integer databaseVersion) {
            this.sqlInitFile = sqlInitFile;
            this.databaseVersion = databaseVersion;
        }
    }

    public static final InitFile INIT_FILE = new InitFile("/sql/init.sql", 1);

    public static final DatabaseInitializer.DatabaseUpgradeHandler<Connection> DATABASE_UPGRADE_HANDLER = new DatabaseInitializer.DatabaseUpgradeHandler<Connection>() {
        @Override
        public Boolean onUpgrade(final com.softwareverde.database.DatabaseConnection<Connection> maintenanceDatabaseConnection, final Integer currentVersion, final Integer requiredVersion) {
            return false;
        }
    };

    public static BchGiftCardsDatabase newInstance(final InitFile sqlInitFile, final DatabaseProperties databaseProperties, final Runnable onShutdownCallback) {
        final DatabaseInitializer<Connection> databaseInitializer = new MysqlDatabaseInitializer(sqlInitFile.sqlInitFile, sqlInitFile.databaseVersion, BchGiftCardsDatabase.DATABASE_UPGRADE_HANDLER);

        try {
            if (databaseProperties.useEmbeddedDatabase()) {
                // Initialize the embedded database...
                final DatabaseCommandLineArguments commandLineArguments = new DatabaseCommandLineArguments();

                Logger.info("[Initializing Database]");
                final EmbeddedMysqlDatabase embeddedMysqlDatabase = new EmbeddedMysqlDatabase(databaseProperties, databaseInitializer, commandLineArguments);

                if (onShutdownCallback != null) {
                    embeddedMysqlDatabase.setShutdownCallback(onShutdownCallback);
                }

                final DatabaseCredentials maintenanceCredentials = databaseInitializer.getMaintenanceCredentials(databaseProperties);
                final MysqlDatabaseConnectionFactory maintenanceDatabaseConnectionFactory = new MysqlDatabaseConnectionFactory(databaseProperties, maintenanceCredentials);
                return new BchGiftCardsDatabase(embeddedMysqlDatabase, maintenanceDatabaseConnectionFactory);
            }
            else {
                // Connect to the remote database...
                final DatabaseCredentials credentials = databaseProperties.getCredentials();
                final DatabaseCredentials rootCredentials = databaseProperties.getRootCredentials();
                final DatabaseCredentials maintenanceCredentials = databaseInitializer.getMaintenanceCredentials(databaseProperties);

                final MysqlDatabaseConnectionFactory rootDatabaseConnectionFactory = new MysqlDatabaseConnectionFactory(databaseProperties.getHostname(), databaseProperties.getPort(), "", rootCredentials.username, rootCredentials.password);
                final MysqlDatabaseConnectionFactory maintenanceDatabaseConnectionFactory = new MysqlDatabaseConnectionFactory(databaseProperties, maintenanceCredentials);

                try (final MysqlDatabaseConnection maintenanceDatabaseConnection = maintenanceDatabaseConnectionFactory.newConnection()) {
                    final Integer databaseVersion = databaseInitializer.getDatabaseVersionNumber(maintenanceDatabaseConnection);
                    if (databaseVersion < 0) {
                        try (final MysqlDatabaseConnection rootDatabaseConnection = rootDatabaseConnectionFactory.newConnection()) {
                            databaseInitializer.initializeSchema(rootDatabaseConnection, databaseProperties);
                        }
                    }
                }
                catch (final DatabaseException exception) {
                    try (final MysqlDatabaseConnection rootDatabaseConnection = rootDatabaseConnectionFactory.newConnection()) {
                        databaseInitializer.initializeSchema(rootDatabaseConnection, databaseProperties);
                    }
                }

                try (final MysqlDatabaseConnection maintenanceDatabaseConnection = maintenanceDatabaseConnectionFactory.newConnection()) {
                    databaseInitializer.initializeDatabase(maintenanceDatabaseConnection);
                }

                return new BchGiftCardsDatabase(new MysqlDatabase(databaseProperties, credentials), maintenanceDatabaseConnectionFactory);
            }
        }
        catch (final DatabaseException exception) {
            Logger.error(exception);
        }

        return null;
    }

    public static MysqlDatabaseConnectionFactory getMaintenanceDatabaseConnectionFactory(final DatabaseProperties databaseProperties) {
        final DatabaseInitializer<Connection> databaseInitializer = new MysqlDatabaseInitializer();
        final DatabaseCredentials maintenanceCredentials = databaseInitializer.getMaintenanceCredentials(databaseProperties);
        return new MysqlDatabaseConnectionFactory(databaseProperties, maintenanceCredentials);
    }

    protected final MysqlDatabase _core;
    protected final MysqlDatabaseConnectionFactory _maintenanceDatabaseConnectionFactory;

    protected BchGiftCardsDatabase(final MysqlDatabase core, final MysqlDatabaseConnectionFactory maintenanceDatabaseConnectionFactory) {
        _core = core;
        _maintenanceDatabaseConnectionFactory = maintenanceDatabaseConnectionFactory;
    }

    @Override
    public DatabaseConnection newConnection() throws DatabaseException {
        return new MysqlDatabaseConnectionWrapper(_core.newConnection());
    }

    public DatabaseConnection getMaintenanceConnection() throws DatabaseException {
        if (_maintenanceDatabaseConnectionFactory == null) { return null; }
        return new MysqlDatabaseConnectionWrapper(_maintenanceDatabaseConnectionFactory.newConnection());
    }

    @Override
    public DatabaseConnectionFactory newConnectionFactory() {
        return new MysqlDatabaseConnectionFactoryWrapper(_core.newConnectionFactory());
    }

    @Override
    public Integer getMaxQueryBatchSize() {
        return 1024;
    }

    @Override
    public void close() throws DatabaseException { }
}
