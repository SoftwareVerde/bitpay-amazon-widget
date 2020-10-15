package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.JsonApiRequest;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.json.Json;

import java.util.ArrayList;
import java.util.List;

public class GetTransactionsRequest extends JsonApiRequest {
    protected List<Sha256Hash> _transactionHashes = new ArrayList<>();

    public List<Sha256Hash> getTransactionHashes() {
        return _transactionHashes;
    }

    public void addTransactionHash(final Sha256Hash transactionHash) {
        _transactionHashes.add(transactionHash);
    }

    public void setTransactionHashes(final List<Sha256Hash> transactionHashes) {
        _transactionHashes = transactionHashes;
    }

    @Override
    protected Json _toJson() throws Exception {
        final Json transactionHashesJson = new Json(true);
        for (final Sha256Hash transactionHash : _transactionHashes) {
            transactionHashesJson.add(transactionHash.toString().toLowerCase());
        }

        final Json jsonRequest = new Json(false);
        jsonRequest.put("txids", transactionHashesJson);
        return jsonRequest;
    }
}
