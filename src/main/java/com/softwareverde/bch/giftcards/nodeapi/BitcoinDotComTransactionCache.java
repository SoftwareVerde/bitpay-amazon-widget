package com.softwareverde.bch.giftcards.nodeapi;

import com.softwareverde.bitcoindotcom.TransactionData;
import com.softwareverde.constable.list.List;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.json.Json;

import java.util.Map;

public interface BitcoinDotComTransactionCache {
    Map<Sha256Hash, TransactionData> getCachedTransactions(List<Sha256Hash> transactionHashes);
    void cacheTransactions(Json json);
}
