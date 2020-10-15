package com.softwareverde.bch.giftcards.nodeapi;

import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.constable.list.List;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;

public interface NodeConnection extends AutoCloseable {
    AddressTransactionsResponse getSpendableTransactions(Address address);
    AddressTransactionsResponse getSpendableTransactions(List<Address> address);
    GetTransactionsResponse getTransactions(List<Sha256Hash> transactionHashes);
    SubmitTransactionResponse submitTransaction(Transaction transaction);

    @Override
    void close();
}
