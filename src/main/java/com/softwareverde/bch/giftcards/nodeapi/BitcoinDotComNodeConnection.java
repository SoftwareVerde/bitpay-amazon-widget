package com.softwareverde.bch.giftcards.nodeapi;

import com.softwareverde.api.ApiConfiguration;
import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.inflater.MasterInflater;
import com.softwareverde.bitcoin.server.database.BatchRunner;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.bitcoin.transaction.output.identifier.TransactionOutputIdentifier;
import com.softwareverde.bitcoindotcom.*;
import com.softwareverde.constable.list.List;
import com.softwareverde.constable.list.immutable.ImmutableList;
import com.softwareverde.constable.list.mutable.MutableList;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.Util;

import java.util.HashMap;
import java.util.Map;

public class BitcoinDotComNodeConnection implements NodeConnection {
    public static final Integer MAX_ITEMS_PER_REQUEST = 20;
    public static final ApiConfiguration API_CONFIGURATION = new ApiConfiguration() {
        @Override
        public String getApiUrl() {
            return "https://rest.bitcoin.com/v2";
        }
    };

    protected final MasterInflater _masterInflater;
    protected final BitcoinDotComTransactionCache _transactionCache;

    protected AddressTransactionsResponse _getSpendableTransactions(final List<Address> addresses) {
        try {
            final GetUtxosForAddressesApiCall getUtxosForAddressesApiCall = new GetUtxosForAddressesApiCall(API_CONFIGURATION);

            final MutableList<GetUtxosForAddressesResponse> responses = new MutableList<GetUtxosForAddressesResponse>();
            final BatchRunner<Address> batchRunner = new BatchRunner<Address>(MAX_ITEMS_PER_REQUEST, true);
            batchRunner.run(addresses, new BatchRunner.Batch<Address>() {
                @Override
                public void run(final List<Address> batchItems) throws Exception {
                    final GetUtxosForAddressesRequest getUtxosForAddressesRequest = new GetUtxosForAddressesRequest();
                    for (final Address address : batchItems) {
                        getUtxosForAddressesRequest.addAddress(address);
                    }
                    final GetUtxosForAddressesResponse response = getUtxosForAddressesApiCall.call(getUtxosForAddressesRequest);
                    synchronized (responses) {
                        responses.add(response);
                    }
                }
            });

            return new AddressTransactionsResponse() {

                @Override
                public Boolean wasSuccessful() {
                    for (final GetUtxosForAddressesResponse response : responses) {
                        if (! response.isSuccess()) { return false; }
                    }
                    return true;
                }

                @Override
                public String getErrorMessage() {
                    for (final GetUtxosForAddressesResponse response : responses) {
                        if (! response.isSuccess()) {
                            return response.getErrorMessage();
                        }
                    }
                    return null;
                }

                @Override
                public List<Address> getAddresses() {
                    return addresses;
                }

                @Override
                public List<Sha256Hash> getTransactionHashes() {
                    final MutableList<Sha256Hash> transactionHashes = new MutableList<Sha256Hash>();
                    for (final GetUtxosForAddressesResponse response : responses) {
                        for (final AddressUtxos addressUtxos : response.getAddressUtxosList()) {
                            for (final Utxo utxo : addressUtxos.getUtxos()) {
                                final Sha256Hash transactionHash = utxo.getTransactionHash();
                                transactionHashes.add(transactionHash);
                            }
                        }
                    }
                    return transactionHashes;
                }

                @Override
                public List<Sha256Hash> getTransactionHashes(final Address address) {
                    final MutableList<Sha256Hash> transactionHashes = new MutableList<Sha256Hash>();
                    for (final GetUtxosForAddressesResponse response : responses) {
                        for (final AddressUtxos addressUtxos : response.getAddressUtxosList()) {
                            if (! Util.areEqual(address, addressUtxos.getAddress())) { continue; }

                            for (final Utxo utxo : addressUtxos.getUtxos()) {
                                final Sha256Hash transactionHash = utxo.getTransactionHash();
                                transactionHashes.add(transactionHash);
                            }
                        }
                    }
                    return transactionHashes;
                }

                @Override
                public List<Utxo> getUtxos() {
                    final MutableList<Utxo> utxos = new MutableList<Utxo>();
                    for (final GetUtxosForAddressesResponse response : responses) {
                        for (final AddressUtxos addressUtxos : response.getAddressUtxosList()) {

                            utxos.addAll(addressUtxos.getUtxos());
                        }
                    }
                    return utxos;
                }

                @Override
                public List<Utxo> getUtxos(final Address address) {
                    final MutableList<Utxo> utxos = new MutableList<Utxo>();
                    for (final GetUtxosForAddressesResponse response : responses) {
                        for (final AddressUtxos addressUtxos : response.getAddressUtxosList()) {
                            if (! Util.areEqual(address, addressUtxos.getAddress())) { continue; }

                            utxos.addAll(addressUtxos.getUtxos());
                        }
                    }
                    return utxos;
                }
            };
        }
        catch (final Exception exception) {
            Logger.warn(exception);
            return null;
        }
    }

    public BitcoinDotComNodeConnection(final MasterInflater masterInflater, final BitcoinDotComTransactionCache transactionCache) {
        _masterInflater = masterInflater;
        _transactionCache = transactionCache;
    }

    @Override
    public AddressTransactionsResponse getSpendableTransactions(final Address address) {
        return _getSpendableTransactions(new ImmutableList<Address>(address));
    }

    @Override
    public AddressTransactionsResponse getSpendableTransactions(final List<Address> address) {
        return _getSpendableTransactions(address);
    }

    @Override
    public GetTransactionsResponse getTransactions(final List<Sha256Hash> transactionHashes) {
        final HashMap<Sha256Hash, TransactionData> cachedTransactionData = new HashMap<Sha256Hash, TransactionData>();
        if (_transactionCache != null) {
            final Map<Sha256Hash, TransactionData> transactionDataMap = _transactionCache.getCachedTransactions(transactionHashes);
            for (final Sha256Hash transactionHash : transactionDataMap.keySet()) {
                final TransactionData transactionData = transactionDataMap.get(transactionHash);
                cachedTransactionData.put(transactionHash, transactionData);
            }

            Logger.info("Loaded " + cachedTransactionData.size() + " transactions from cache.");
        }

        final MutableList<Sha256Hash> missingTransactionHashes = new MutableList<Sha256Hash>();
        for (final Sha256Hash transactionHash : transactionHashes) {
            if (! cachedTransactionData.containsKey(transactionHash)) {
                missingTransactionHashes.add(transactionHash);
            }
        }

        try {
            final MutableList<com.softwareverde.bitcoindotcom.GetTransactionsResponse> responses = new MutableList<com.softwareverde.bitcoindotcom.GetTransactionsResponse>();
            if (! missingTransactionHashes.isEmpty()) {
                Logger.info("Requesting " + missingTransactionHashes.getCount() + " transaction details.");

                final GetTransactionsApiCall getTransactionsApiCall = new GetTransactionsApiCall(API_CONFIGURATION);
                final BatchRunner<Sha256Hash> batchRunner = new BatchRunner<Sha256Hash>(MAX_ITEMS_PER_REQUEST, true);
                batchRunner.run(missingTransactionHashes, new BatchRunner.Batch<Sha256Hash>() {
                    @Override
                    public void run(final List<Sha256Hash> batchItems) throws Exception {
                        final GetTransactionsRequest getTransactionsRequest = new GetTransactionsRequest();
                        for (final Sha256Hash transactionHash : batchItems) {
                            getTransactionsRequest.addTransactionHash(transactionHash);
                        }

                        final com.softwareverde.bitcoindotcom.GetTransactionsResponse response = getTransactionsApiCall.call(getTransactionsRequest);
                        synchronized (responses) {
                            responses.add(response);

                            if (_transactionCache != null) {
                                _transactionCache.cacheTransactions(response.getJson());
                            }
                        }
                    }
                });
            }

            return new GetTransactionsResponse() {
                @Override
                public Boolean wasSuccessful() {
                    for (final com.softwareverde.bitcoindotcom.GetTransactionsResponse response : responses) {
                        if (! response.isSuccess()) { return false; }
                    }
                    return true;
                }

                @Override
                public String getErrorMessage() {
                    for (final com.softwareverde.bitcoindotcom.GetTransactionsResponse response : responses) {
                        if (! response.isSuccess()) {
                            return response.getErrorMessage();
                        }
                    }
                    return null;
                }

                @Override
                public Map<Sha256Hash, Transaction> getTransactions() {
                    final HashMap<Sha256Hash, Transaction> transactionHashMap = new HashMap<Sha256Hash, Transaction>();

                    for (final com.softwareverde.bitcoindotcom.GetTransactionsResponse response : responses) {
                        final java.util.List<Transaction> transactions = response.getTransactions();
                        for (final Transaction transaction : transactions) {
                            final Sha256Hash transactionHash = transaction.getHash();
                            transactionHashMap.put(transactionHash, transaction);
                        }
                    }

                    for (final Sha256Hash transactionHash : cachedTransactionData.keySet()) {
                        final TransactionData transactionData = cachedTransactionData.get(transactionHash);
                        transactionHashMap.put(transactionHash, transactionData.transaction);
                    }

                    return transactionHashMap;
                }

                @Override
                public Map<TransactionOutputIdentifier, Address> getPreviousOutputAddresses() {
                    final HashMap<TransactionOutputIdentifier, Address> outputsAddressesMap = new HashMap<TransactionOutputIdentifier, Address>();

                    for (final com.softwareverde.bitcoindotcom.GetTransactionsResponse response : responses) {
                        final Map<TransactionOutputIdentifier, Address> map = response.getPreviousOutputAddresses();
                        outputsAddressesMap.putAll(map);
                    }

                    for (final Sha256Hash transactionHash : cachedTransactionData.keySet()) {
                        final TransactionData transactionData = cachedTransactionData.get(transactionHash);
                        outputsAddressesMap.putAll(transactionData.previousOutputAddresses);
                    }

                    return outputsAddressesMap;
                }

                @Override
                public Map<TransactionOutputIdentifier, Long> getPreviousOutputAmounts() {
                    final HashMap<TransactionOutputIdentifier, Long> outputsAmountsMap = new HashMap<TransactionOutputIdentifier, Long>();

                    for (final com.softwareverde.bitcoindotcom.GetTransactionsResponse response : responses) {
                        final Map<TransactionOutputIdentifier, Long> map = response.getPreviousOutputAmounts();
                        outputsAmountsMap.putAll(map);
                    }

                    for (final Sha256Hash transactionHash : cachedTransactionData.keySet()) {
                        final TransactionData transactionData = cachedTransactionData.get(transactionHash);
                        outputsAmountsMap.putAll(transactionData.previousOutputAmounts);
                    }

                    return outputsAmountsMap;
                }
            };
        }
        catch (final Exception exception) {
            Logger.warn(exception);
            return null;
        }
    }

    @Override
    public SubmitTransactionResponse submitTransaction(final Transaction transaction) {
        try {
            final SendTransactionsRequest sendTransactionsRequest = new SendTransactionsRequest();
            sendTransactionsRequest.addTransaction(transaction);
            final SendTransactionsApiCall sendTransactionsApiCall = new SendTransactionsApiCall(API_CONFIGURATION);
            final SendTransactionsResponse sendTransactionsResponse = sendTransactionsApiCall.call(sendTransactionsRequest);

            return new SubmitTransactionResponse() {
                @Override
                public Boolean wasSuccessful() {
                    return sendTransactionsResponse.isSuccess();
                }

                @Override
                public String getErrorMessage() {
                    return sendTransactionsResponse.getErrorMessage();
                }
            };
        }
        catch (final Exception exception) {
            Logger.warn(exception);
            return null;
        }
    }

    @Override
    public void close() { }
}
