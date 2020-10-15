package com.softwareverde.bitcoindotcom;

import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.address.AddressInflater;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.json.Json;
import com.softwareverde.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class AddressUtxos {
    protected List<Utxo> _utxos;
    protected Address _address;
    protected String _lockingScriptHex;
    protected String _lockingScriptAssembly;

    public static AddressUtxos fromJson(final Json json) {
        final AddressUtxos addressUtxos = new AddressUtxos();

        final Json utxosJson = json.getOrNull("utxos", Json.Types.ARRAY);
        if (utxosJson == null) {
            Logger.warn("Invalid UTXO List JSON: expected utxos to be an array.");
            return null;
        }
        final List<Utxo> utxos = new ArrayList<Utxo>();
        for (int i = 0; i < utxosJson.length(); ++i) {
            final Json utxoJson = utxosJson.getOrNull(i, Json.Types.OBJECT);
            if (utxoJson == null) {
                Logger.warn("Invalid UTXO List JSON: null UTXO object in position " + i);
                return null;
            }

            final String transactionHashString = utxoJson.getOrNull("txid", Json.Types.STRING);
            final Integer outputIndex = utxoJson.getOrNull("vout", Json.Types.INTEGER);
            final Double amount = utxoJson.getOrNull("amount", Json.Types.DOUBLE);
            final Long satoshis = utxoJson.getOrNull("satoshis", Json.Types.LONG);
            final Long blockHeight = utxoJson.getOrNull("height", Json.Types.LONG);
            final Long confirmations = utxoJson.getOrNull("confirmations", Json.Types.LONG);

            final Sha256Hash transactionHash = Sha256Hash.fromHexString(transactionHashString);

            final Utxo utxo = new Utxo();
            utxo.setTransactionHash(transactionHash);
            utxo.setOutputIndex(outputIndex);
            utxo.setAmount(amount);
            utxo.setSatoshis(satoshis);
            utxo.setBlockHeight(blockHeight);
            utxo.setConfirmations(confirmations);

            utxos.add(utxo);
        }

        final AddressInflater addressInflater = new AddressInflater();
        final String addressString = json.getOrNull("legacyAddress", Json.Types.STRING);

        addressUtxos._utxos = utxos;
        addressUtxos._address = addressInflater.fromBase58Check(addressString);
        addressUtxos._lockingScriptHex = json.getOrNull("scriptPubKey", Json.Types.STRING);
        addressUtxos._lockingScriptAssembly = json.getOrNull("asm", Json.Types.STRING);

        return addressUtxos;
    }

    public List<Utxo> getUtxos() {
        return _utxos;
    }

    public Address getAddress() {
        return _address;
    }

    public String getLockingScriptHex() {
        return _lockingScriptHex;
    }

    public String getLockingScriptAssembly() {
        return _lockingScriptAssembly;
    }
}
