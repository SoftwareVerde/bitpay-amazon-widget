package com.softwareverde.bch.giftcards.nodeapi;


import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoindotcom.Utxo;
import com.softwareverde.constable.list.List;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;

public interface AddressTransactionsResponse extends Response {
    List<Address> getAddresses();
    List<Sha256Hash> getTransactionHashes();
    List<Sha256Hash> getTransactionHashes(Address address);
    List<Utxo> getUtxos();
    List<Utxo> getUtxos(Address address);
}
