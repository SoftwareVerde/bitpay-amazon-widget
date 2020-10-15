package com.softwareverde.bch.giftcards.webserver;

import com.softwareverde.bch.giftcards.configuration.BitcoinProperties;
import com.softwareverde.bch.giftcards.configuration.ServerProperties;
import com.softwareverde.bch.giftcards.database.BchGiftCardsDatabase;
import com.softwareverde.bch.giftcards.database.DatabaseManager;
import com.softwareverde.bch.giftcards.nodeapi.BitcoinDotComNodeConnection;
import com.softwareverde.bch.giftcards.nodeapi.BitcoinDotComTransactionCache;
import com.softwareverde.bch.giftcards.nodeapi.BitcoinDotComTransactionDatabaseCache;
import com.softwareverde.bch.giftcards.nodeapi.NodeConnection;
import com.softwareverde.bitcoin.PriceIndexer;
import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.inflater.MasterInflater;
import com.softwareverde.bitcoin.server.database.DatabaseConnection;
import com.softwareverde.bitcoin.wallet.Wallet;
import com.softwareverde.concurrent.pool.ThreadPool;
import com.softwareverde.database.DatabaseException;

public class Environment implements com.softwareverde.http.server.servlet.routed.Environment {
    protected final ServerProperties _serverProperties;
    protected final BitcoinProperties _bitcoinProperties;
    protected final ThreadPool _threadPool;
    protected final MasterInflater _masterInflater;
    protected final BchGiftCardsDatabase _database;
    protected final Wallet _spvWallet;
    protected final PriceIndexer _priceIndexer;

    public Environment(final ServerProperties serverProperties, final BitcoinProperties bitcoinProperties, final ThreadPool threadPool, final BchGiftCardsDatabase devTokenDatabase, final MasterInflater masterInflater, final PriceIndexer priceIndexer) {
        _serverProperties = serverProperties;
        _bitcoinProperties = bitcoinProperties;
        _threadPool = threadPool;
        _masterInflater = masterInflater;
        _database = devTokenDatabase;

        _spvWallet = new Wallet();

        _priceIndexer = priceIndexer;
    }

    public ServerProperties getServerProperties() {
        return _serverProperties;
    }

    public BitcoinProperties getBitcoinProperties() {
        return _bitcoinProperties;
    }

    public ThreadPool getThreadPool() {
        return _threadPool;
    }

    public NodeConnection newNodeConnection() {
        final BitcoinDotComTransactionCache transactionCache = new BitcoinDotComTransactionDatabaseCache(this);
        return new BitcoinDotComNodeConnection(_masterInflater, transactionCache);
    }

    public MasterInflater getMasterInflater() {
        return _masterInflater;
    }

    public DatabaseManager newDatabaseManager() throws DatabaseException {
        final DatabaseConnection databaseConnection = _database.newConnection();
        return new DatabaseManager(databaseConnection, _masterInflater);
    }

    public Address getDestinationAddress() {
        return _serverProperties.getDestinationAddress();
    }

    public Wallet getSpvWallet() {
        return _spvWallet;
    }

    public PriceIndexer getPriceIndexer() {
        return _priceIndexer;
    }
}
