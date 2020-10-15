package com.softwareverde.bch.giftcards;

import com.softwareverde.bch.giftcards.configuration.BitcoinProperties;
import com.softwareverde.bch.giftcards.configuration.Configuration;
import com.softwareverde.bch.giftcards.configuration.DatabaseProperties;
import com.softwareverde.bch.giftcards.configuration.ServerProperties;
import com.softwareverde.bch.giftcards.database.BchGiftCardsDatabase;
import com.softwareverde.bch.giftcards.database.DatabaseManager;
import com.softwareverde.bch.giftcards.gmail.EmailClient;
import com.softwareverde.bch.giftcards.webserver.Environment;
import com.softwareverde.bch.giftcards.webserver.NewAddressCreatedCallback;
import com.softwareverde.bch.giftcards.webserver.WebServer;
import com.softwareverde.bitcoin.CoreInflater;
import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.inflater.MasterInflater;
import com.softwareverde.bitcoin.server.configuration.SeedNodeProperties;
import com.softwareverde.bitcoin.server.database.DatabaseConnection;
import com.softwareverde.bitcoin.server.database.pool.DatabaseConnectionPool;
import com.softwareverde.bitcoin.server.module.spv.SpvModule;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.bitcoin.wallet.Wallet;
import com.softwareverde.bitcoindotcom.BitcoinDotComPriceIndexer;
import com.softwareverde.concurrent.pool.MainThreadPool;
import com.softwareverde.constable.list.List;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.cryptography.secp256k1.key.PrivateKey;
import com.softwareverde.database.DatabaseException;
import com.softwareverde.logging.LogLevel;
import com.softwareverde.logging.Logger;
import com.softwareverde.logging.slf4j.Slf4jLogFactory;

import java.io.File;

public class Main {
    protected static void exitFailure() {
        System.exit(1);
    }

    protected static void exitSuccess() {
        System.exit(0);
    }

    protected static void printError(final String errorMessage) {
        System.err.println(errorMessage);
    }

    protected static void printUsage() {
        Main.printError("Usage: java -jar " + System.getProperty("java.class.path") + " <configuration-file>");
    }

    protected static BchGiftCardsDatabase loadDatabase(final DatabaseProperties databaseProperties) {
        return BchGiftCardsDatabase.newInstance(BchGiftCardsDatabase.INIT_FILE, databaseProperties, null);
    }

    protected static Configuration loadConfigurationFile(final String configurationFilename, final MasterInflater masterInflater) {
        final File configurationFile =  new File(configurationFilename);
        if (! configurationFile.isFile()) {
            Main.printError("Invalid configuration file: " + configurationFilename);
            Main.exitFailure();
        }

        return new Configuration(configurationFile, masterInflater);
    }

    public static void main(final String[] commandLineArguments) {
        Logger.setLogFactory(new Slf4jLogFactory());
        Logger.setLogLevel(LogLevel.DEBUG);

        if (commandLineArguments.length != 1) {
            Main.printUsage();
            Main.exitFailure();
        }

        final MasterInflater masterInflater = new CoreInflater();
        final String configurationFilename = commandLineArguments[0];
        final Configuration configuration = Main.loadConfigurationFile(configurationFilename, masterInflater);

        Logger.info("[Starting Database]");
        final BchGiftCardsDatabase database = Main.loadDatabase(configuration.getDatabaseProperties());

        final ServerProperties serverProperties = configuration.getServerProperties();
        final BitcoinProperties bitcoinProperties = configuration.getBitcoinProperties();


        final BitcoinDotComPriceIndexer priceIndexer = new BitcoinDotComPriceIndexer();

        final MainThreadPool threadPool = new MainThreadPool(64, 1000L);
        final Environment environment = new Environment(serverProperties, bitcoinProperties, threadPool, database, masterInflater, priceIndexer);

        {
            if (serverProperties.getDestinationAddress() == null) {
                Logger.error("Invalid destination address in configuration.");
                Main.exitFailure();
            }
        }

        final Wallet spvWallet = environment.getSpvWallet();

        // Add all previous private keys to the SPV wallet...
        try (final DatabaseManager databaseManager = environment.newDatabaseManager()) {
            { // Redemption PrivateKeys
                final List<Address> redemptionAddresses = databaseManager.getRedemptionAddresses();
                for (final Address address : redemptionAddresses) {
                    final PrivateKey privateKey = databaseManager.getPrivateKey(address);
                    spvWallet.addPrivateKey(privateKey);
                }
            }
        }
        catch (final DatabaseException exception) {
            Logger.error(exception);
            Main.exitFailure();
        }

        final Thread spvThread;
        final SpvModule spvModule;
        {
            Logger.info("[Starting SPV Module]");
            final SeedNodeProperties[] seedNodeProperties;
            {
                final List<SeedNodeProperties> seedNodePropertiesList = bitcoinProperties.getSeedNodeProperties();
                final int peerCount = seedNodePropertiesList.getCount();
                seedNodeProperties = new SeedNodeProperties[peerCount];
                for (int i = 0; i < peerCount; ++i) {
                    seedNodeProperties[i] = seedNodePropertiesList.get(i);
                }
            }

            final DatabaseConnectionPool databaseConnectionPool = new DatabaseConnectionPool() {
                @Override
                public DatabaseConnection newConnection() throws DatabaseException {
                    return database.newConnection();
                }

                @Override
                public void close() throws DatabaseException { }
            };

            final com.softwareverde.bitcoin.server.Environment serverEnvironment = new com.softwareverde.bitcoin.server.Environment(database, databaseConnectionPool);
            spvModule = new SpvModule(serverEnvironment, seedNodeProperties, 8, spvWallet);
            spvThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    spvModule.initialize();
                    spvModule.loop();
                }
            });
        }

        {
            Logger.info("[Updating Wallet]");

            final Double dollarsPerBitcoin = priceIndexer.getDollarsPerBitcoin();

            Logger.info("BCH Price: " + dollarsPerBitcoin);
        }

        final EmailClient emailClient;
        {
            emailClient = new EmailClient(environment);
        }

        final GiftCardService giftCardService;
        {
            Logger.info("[Starting Token Service]");
            giftCardService = new GiftCardService(environment, priceIndexer, emailClient);
            giftCardService.start();
            giftCardService.wakeUp();
        }

        final WebServer webServer = new WebServer(environment, new NewAddressCreatedCallback() {
            @Override
            public void newAddressCreated(final Address address) {
                spvModule.onWalletKeysUpdated();
            }
        });

        spvModule.setNewTransactionCallback(new SpvModule.NewTransactionCallback() {
            @Override
            public void onNewTransactionReceived(final Transaction transaction) {
                final Sha256Hash transactionHash = transaction.getHash();
                Logger.debug("New Transaction: " + transactionHash);

                webServer.onNewTransaction(transaction);
                giftCardService.onNewTransaction(transactionHash);
                giftCardService.wakeUp();
            }
        });

        Logger.info("[Starting Web Server]");
        webServer.start();

        Logger.info("[Starting SPV Wallet]");
        spvThread.start();

        while (true) {
            try { Thread.sleep(1000); } catch (final Exception exception) { break; }
        }

        Logger.info("[Shutting Down]");
        spvThread.interrupt();
        webServer.stop();
        giftCardService.stop();
        threadPool.stop();

        try { spvThread.join(); } catch (final Exception exception) { }
    }
}
