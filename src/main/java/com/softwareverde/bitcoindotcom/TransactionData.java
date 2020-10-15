package com.softwareverde.bitcoindotcom;

import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.bitcoin.transaction.output.identifier.TransactionOutputIdentifier;

import java.util.Map;

public class TransactionData {
    public final Transaction transaction;
    public final Map<TransactionOutputIdentifier, Address> previousOutputAddresses;
    public final Map<TransactionOutputIdentifier, Long> previousOutputAmounts;

    public TransactionData(final Transaction transaction, final Map<TransactionOutputIdentifier, Address> previousOutputAddresses, final Map<TransactionOutputIdentifier, Long> previousOutputAmounts) {
        this.transaction = transaction;
        this.previousOutputAddresses = previousOutputAddresses;
        this.previousOutputAmounts = previousOutputAmounts;
    }
}
