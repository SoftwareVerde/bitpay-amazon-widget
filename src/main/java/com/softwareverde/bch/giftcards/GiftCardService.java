package com.softwareverde.bch.giftcards;

import com.softwareverde.bch.giftcards.database.DatabaseManager;
import com.softwareverde.bch.giftcards.gmail.EmailClient;
import com.softwareverde.bch.giftcards.nodeapi.AddressTransactionsResponse;
import com.softwareverde.bch.giftcards.nodeapi.GetTransactionsResponse;
import com.softwareverde.bch.giftcards.nodeapi.NodeConnection;
import com.softwareverde.bch.giftcards.nodeapi.SubmitTransactionResponse;
import com.softwareverde.bch.giftcards.webserver.Environment;
import com.softwareverde.bitcoin.PriceIndexer;
import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.inflater.MasterInflater;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.bitcoin.transaction.TransactionDeflater;
import com.softwareverde.bitcoin.transaction.input.TransactionInput;
import com.softwareverde.bitcoin.transaction.output.TransactionOutput;
import com.softwareverde.bitcoin.transaction.output.identifier.TransactionOutputIdentifier;
import com.softwareverde.bitcoin.wallet.Wallet;
import com.softwareverde.bitcoin.wallet.utxo.SpendableTransactionOutput;
import com.softwareverde.bitcoindotcom.Utxo;
import com.softwareverde.concurrent.service.SleepyService;
import com.softwareverde.constable.bytearray.ByteArray;
import com.softwareverde.constable.list.List;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.cryptography.secp256k1.key.PrivateKey;
import com.softwareverde.database.DatabaseException;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GiftCardService extends SleepyService {
    protected final Environment _environment;
    protected final PriceIndexer _priceIndexer;
    protected final EmailClient _emailClient;

    protected final ConcurrentHashMap<Sha256Hash, Integer> _newTransactions = new ConcurrentHashMap<Sha256Hash, Integer>();

    public static void loadWalletWithUnspentTransactionOutputs(final Wallet wallet, final Address address, final DatabaseManager databaseManager, final NodeConnection nodeConnection) throws DatabaseException {
        final AddressTransactionsResponse addressTransactionsResponse = nodeConnection.getSpendableTransactions(address);
        final GetTransactionsResponse getTransactionsResponse = nodeConnection.getTransactions(addressTransactionsResponse.getTransactionHashes());
        final Map<Sha256Hash, Transaction> transactions = getTransactionsResponse.getTransactions();

        final List<Utxo> utxos = addressTransactionsResponse.getUtxos();
        final HashSet<TransactionOutputIdentifier> unspentTransactionOutputIdentifiers = new HashSet<TransactionOutputIdentifier>();
        for (final Utxo utxo : utxos) {
            final Sha256Hash transactionHash = utxo.getTransactionHash();
            final Integer outputIndex = utxo.getOutputIndex();

            final TransactionOutputIdentifier transactionOutputIdentifier = new TransactionOutputIdentifier(transactionHash, outputIndex);
            unspentTransactionOutputIdentifiers.add(transactionOutputIdentifier);
        }

        for (final Sha256Hash transactionHash : transactions.keySet()) {
            final Boolean isAlreadyRedeemed = databaseManager.wasTransactionProcessed(transactionHash);
            if (isAlreadyRedeemed) { continue; }

            final Transaction transaction = transactions.get(transactionHash);
            wallet.addTransaction(transaction);

            final List<TransactionOutput> transactionOutputs = transaction.getTransactionOutputs();
            for (int outputIndex = 0; outputIndex < transactionOutputs.getCount(); ++outputIndex) {
                final TransactionOutputIdentifier transactionOutputIdentifier = new TransactionOutputIdentifier(transactionHash, outputIndex);
                if (! unspentTransactionOutputIdentifiers.contains(transactionOutputIdentifier)) {
                    wallet.markTransactionOutputAsSpent(transactionHash, outputIndex);
                }
            }
        }
    }

    protected Boolean _broadcastTransaction(final Transaction transaction) {
        try (final NodeConnection nodeConnection = _environment.newNodeConnection()) {
            final SubmitTransactionResponse submitTransactionResponse = nodeConnection.submitTransaction(transaction);

            if (! submitTransactionResponse.wasSuccessful()) {
                final MasterInflater masterInflater = _environment.getMasterInflater();
                final TransactionDeflater transactionDeflater = masterInflater.getTransactionDeflater();
                final ByteArray transactionBytes = transactionDeflater.toBytes(transaction);
                Logger.warn("Unable to send Transaction: " + transactionBytes);

                return false;
            }

            return true;
        }
    }

    protected Boolean _claimTransactionAndDistributeGiftCard(final Transaction transaction, final PrivateKey privateKey) {
        final Wallet wallet = new Wallet();
        wallet.addPrivateKey(privateKey);
        wallet.addTransaction(transaction);

        final Long dustAmount = wallet.getDustThreshold(true);

        final Long netAmountReceived = wallet.getBalance();

        if (netAmountReceived < dustAmount) {
            final Sha256Hash transactionHash = transaction.getHash();
            Logger.info("Transaction " + transactionHash + " net donation amount below dust threshold. Consider increasing satoshisPerToken requirement.");
            return true;
        }

        // TODO: send gift card
        Logger.info("Received " + netAmountReceived + " satoshis.");
        return true;
    }

    protected void _processRedemptionAddresses() throws DatabaseException {
        try (final DatabaseManager databaseManager = _environment.newDatabaseManager()) {
            final List<Address> redemptionAddresses = databaseManager.getRedemptionAddresses();

            final List<Sha256Hash> transactionHashes;
            final AddressTransactionsResponse addressTransactionsResponse;
            try (final NodeConnection nodeConnection = _environment.newNodeConnection()) {
                addressTransactionsResponse = nodeConnection.getSpendableTransactions(redemptionAddresses);

                if (! addressTransactionsResponse.wasSuccessful()) {
                    Logger.debug("Unable to load Transactions for donation addresses.");
                    return;
                }

                transactionHashes = addressTransactionsResponse.getTransactionHashes();
            }

            final Map<Sha256Hash, Transaction> transactions;
            {
                final GetTransactionsResponse getTransactionsResponse;
                try (final NodeConnection nodeConnection = _environment.newNodeConnection()) {
                    getTransactionsResponse = nodeConnection.getTransactions(transactionHashes);
                }
                if (! getTransactionsResponse.wasSuccessful()) {
                    Logger.debug("Unable to load Transactions for donation addresses.");
                    return;
                }

                transactions = getTransactionsResponse.getTransactions();
            }

            for (final Address redemptionAddress : redemptionAddresses) {
                final List<Sha256Hash> addressTransactionHashes = addressTransactionsResponse.getTransactionHashes(redemptionAddress);
                for (final Sha256Hash transactionHash : addressTransactionHashes) {
                    _newTransactions.remove(transactionHash); // Dequeue the Transaction for retries...

                    final Transaction transaction = transactions.get(transactionHash);
                    final Boolean transactionHasBeenProcessed = databaseManager.wasTransactionProcessed(transactionHash);
                    if (transactionHasBeenProcessed) { continue; }

                    final PrivateKey privateKey = databaseManager.getPrivateKey(redemptionAddress);
                    final Boolean wasProcessedSuccessfully = _claimTransactionAndDistributeGiftCard(transaction, privateKey);

                    if (wasProcessedSuccessfully) {
                        databaseManager.markTransactionAsProcessed(transactionHash);
                    }
                }
            }
        }
    }

    @Override
    protected void _onStart() { }

    @Override
    protected Boolean _run() {
        try {
            _processRedemptionAddresses();

            if (_newTransactions.isEmpty()) {
                return false;
            }

            { // While there were new transactions announced that weren't successfully processed then try multiple times, after a delay.
                for (final Sha256Hash transactionHash : _newTransactions.keySet()) {
                    final Integer attemptCount = _newTransactions.get(transactionHash);
                    if (attemptCount == null) { continue; }

                    if (attemptCount > 2) {
                        _newTransactions.remove(transactionHash);
                        continue;
                    }

                    _newTransactions.put(transactionHash, (attemptCount + 1));
                }

                Thread.sleep(5000L);

                return true;
            }
        }
        catch (final Exception exception) {
            Logger.debug(exception);
            return false;
        }
    }

    @Override
    protected void _onSleep() { }

    public GiftCardService(final Environment environment, final PriceIndexer priceIndexer, final EmailClient emailClient) {
        _environment = environment;
        _priceIndexer = priceIndexer;
        _emailClient = emailClient;
    }

    public void onNewTransaction(final Sha256Hash transactionHash) {
        _newTransactions.put(transactionHash, 0);
    }
}
