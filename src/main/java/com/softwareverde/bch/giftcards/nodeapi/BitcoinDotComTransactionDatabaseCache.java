package com.softwareverde.bch.giftcards.nodeapi;

import com.softwareverde.bch.giftcards.database.DatabaseManager;
import com.softwareverde.bch.giftcards.webserver.Environment;
import com.softwareverde.bitcoin.server.database.DatabaseConnection;
import com.softwareverde.bitcoin.server.database.query.Query;
import com.softwareverde.bitcoin.server.database.query.ValueExtractor;
import com.softwareverde.bitcoindotcom.GetTransactionsResponse;
import com.softwareverde.bitcoindotcom.TransactionData;
import com.softwareverde.constable.list.List;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.database.DatabaseException;
import com.softwareverde.database.row.Row;
import com.softwareverde.json.Json;
import com.softwareverde.logging.Logger;

import java.util.HashMap;
import java.util.Map;

public class BitcoinDotComTransactionDatabaseCache implements BitcoinDotComTransactionCache {
    protected final Environment _environment;

    public BitcoinDotComTransactionDatabaseCache(final Environment environment) {
        _environment = environment;
    }

    @Override
    public Map<Sha256Hash, TransactionData> getCachedTransactions(final List<Sha256Hash> transactionHashes) {
        try (final DatabaseManager databaseManager = _environment.newDatabaseManager()) {
            final DatabaseConnection databaseConnection = databaseManager.getDatabaseConnection();

            final HashMap<Sha256Hash, TransactionData> transactions = new HashMap<Sha256Hash, TransactionData>();

            final java.util.List<Row> rows = databaseConnection.query(
                new Query("SELECT transaction_hash, data FROM cached_transaction_data WHERE transaction_hash IN (?)")
                    .setInClauseParameters(transactionHashes, ValueExtractor.SHA256_HASH)
            );
            for (final Row row : rows) {
                final Sha256Hash transactionHash = Sha256Hash.wrap(row.getBytes("transaction_hash"));
                final String transactionDataString = row.getString("data");
                final Json transactionDataJson = Json.parse(transactionDataString);
                final TransactionData transactionData = GetTransactionsResponse.inflateTransactionDataFromJson(transactionDataJson);
                if (transactionData == null) { continue; }

                transactions.put(transactionHash, transactionData);
            }

            return transactions;
        }
        catch (final DatabaseException databaseException) {
            return new HashMap<Sha256Hash, TransactionData>(0);
        }
    }

    @Override
    public void cacheTransactions(final Json json) {
        try (final DatabaseManager databaseManager = _environment.newDatabaseManager()) {
            final DatabaseConnection databaseConnection = databaseManager.getDatabaseConnection();

            final Integer itemCount = json.length();
            for (int i = 0; i < itemCount; ++i) {
                final Json transactionObjectJson = json.get(i);

                final Sha256Hash transactionHash = GetTransactionsResponse.getTransactionHashFromJson(transactionObjectJson);
                final String jsonString = transactionObjectJson.toString();

                databaseConnection.executeSql(
                    new Query("INSERT IGNORE INTO cached_transaction_data (transaction_hash, data) VALUES (?, ?)")
                        .setParameter(transactionHash)
                        .setParameter(jsonString)
                );
            }
        }
        catch (final DatabaseException databaseException) {
            Logger.debug(databaseException);
        }
    }
}
