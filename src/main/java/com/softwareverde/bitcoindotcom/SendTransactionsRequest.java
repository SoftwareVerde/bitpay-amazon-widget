package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.JsonApiRequest;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.bitcoin.transaction.TransactionDeflater;
import com.softwareverde.constable.bytearray.ByteArray;
import com.softwareverde.json.Json;

import java.util.ArrayList;
import java.util.List;

public class SendTransactionsRequest extends JsonApiRequest {
    protected List<Transaction> _transactions = new ArrayList<>();

    public List<Transaction> getTransactions() {
        return _transactions;
    }

    public void addTransaction(final Transaction transaction) {
        _transactions.add(transaction);
    }

    public void setTransactions(final List<Transaction> transactions) {
        _transactions = transactions;
    }

    @Override
    protected Json _toJson() throws Exception {
        final TransactionDeflater transactionDeflater = new TransactionDeflater();
        final Json hexes = new Json(true);
        for (final Transaction transaction : _transactions) {
            final ByteArray transactionBytes = transactionDeflater.toBytes(transaction);
            hexes.add(transactionBytes.toString());
        }

        final Json jsonRequest = new Json(false);
        jsonRequest.put("hexes", hexes);
        return jsonRequest;
    }
}
