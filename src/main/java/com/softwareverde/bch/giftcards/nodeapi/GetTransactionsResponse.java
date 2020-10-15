package com.softwareverde.bch.giftcards.nodeapi;

import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.bitcoin.transaction.output.identifier.TransactionOutputIdentifier;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;

import java.util.Map;

public interface GetTransactionsResponse extends Response {
    Map<Sha256Hash, Transaction> getTransactions();
    Map<TransactionOutputIdentifier, Address> getPreviousOutputAddresses();
    Map<TransactionOutputIdentifier, Long> getPreviousOutputAmounts();
}
