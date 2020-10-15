package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.JsonApiResponse;
import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.address.AddressInflater;
import com.softwareverde.bitcoin.transaction.MutableTransaction;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.bitcoin.transaction.input.MutableTransactionInput;
import com.softwareverde.bitcoin.transaction.input.TransactionInput;
import com.softwareverde.bitcoin.transaction.locktime.ImmutableLockTime;
import com.softwareverde.bitcoin.transaction.locktime.ImmutableSequenceNumber;
import com.softwareverde.bitcoin.transaction.locktime.SequenceNumber;
import com.softwareverde.bitcoin.transaction.output.MutableTransactionOutput;
import com.softwareverde.bitcoin.transaction.output.TransactionOutput;
import com.softwareverde.bitcoin.transaction.output.identifier.TransactionOutputIdentifier;
import com.softwareverde.bitcoin.transaction.script.locking.ImmutableLockingScript;
import com.softwareverde.bitcoin.transaction.script.locking.LockingScript;
import com.softwareverde.bitcoin.transaction.script.unlocking.ImmutableUnlockingScript;
import com.softwareverde.bitcoin.transaction.script.unlocking.UnlockingScript;
import com.softwareverde.constable.bytearray.ByteArray;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.http.HttpResponse;
import com.softwareverde.json.Json;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetTransactionsResponse extends JsonApiResponse {
    public final List<Transaction> _transactions = new ArrayList<Transaction>(0);
    public final Map<TransactionOutputIdentifier, Address> _previousOutputAddresses = new HashMap<TransactionOutputIdentifier, Address>(0);
    public final Map<TransactionOutputIdentifier, Long> _previousOutputAmounts = new HashMap<TransactionOutputIdentifier, Long>(0);
    protected final String _errorMessage;

    protected static TransactionInput inflateTransactionInputFromJson(final Json json) {
        final Sha256Hash previousTransactionHash = Sha256Hash.fromHexString(json.getString("txid"));
        final Integer previousOutputIndex = json.getInteger("vout");
        final Long sequenceNumberLong = json.getLong("sequence");

        final UnlockingScript unlockingScript;
        {
            final Json scriptSig = json.get("scriptSig");
            final ByteArray unlockingScriptBytes = ByteArray.fromHexString(scriptSig.getString("hex"));
            unlockingScript = new ImmutableUnlockingScript(unlockingScriptBytes);
        }

        final SequenceNumber sequenceNumber = new ImmutableSequenceNumber(sequenceNumberLong);
        final MutableTransactionInput transactionInput = new MutableTransactionInput();
        transactionInput.setPreviousOutputTransactionHash(previousTransactionHash);
        transactionInput.setPreviousOutputIndex(previousOutputIndex);
        transactionInput.setSequenceNumber(sequenceNumber);
        transactionInput.setUnlockingScript(unlockingScript);
        return transactionInput;
    }

    protected static TransactionOutput inflateTransactionOutputFromJson(final Json json, final Integer outputIndex) {
        final Double value = json.getDouble("value");
        final Long amount = (long) ((value * Transaction.SATOSHIS_PER_BITCOIN) + 0.5D);

        final LockingScript lockingScript;
        {
            final Json scriptPubKey = json.get("scriptPubKey");
            final ByteArray lockingScriptBytes = ByteArray.fromHexString(scriptPubKey.getString("hex"));
            lockingScript = new ImmutableLockingScript(lockingScriptBytes);
        }

        final MutableTransactionOutput transactionOutput = new MutableTransactionOutput();
        transactionOutput.setAmount(amount);
        transactionOutput.setLockingScript(lockingScript);
        transactionOutput.setIndex(outputIndex);
        return transactionOutput;
    }

    protected static Transaction inflateTransactionFromJson(final Json json) {
        final Sha256Hash expectedTransactionHash = Sha256Hash.fromHexString(json.getString("txid"));
        final Long version = json.getLong("version");
        final Long lockTime = json.getLong("locktime");

        final MutableTransaction transaction = new MutableTransaction();
        transaction.setVersion(version);
        transaction.setLockTime(new ImmutableLockTime(lockTime));

        {
            final Json transactionInputsJson = json.get("vin");
            for (int i = 0; i < transactionInputsJson.length(); ++i) {
                final Json transactionInputJson = transactionInputsJson.get(i);
                final TransactionInput transactionInput = GetTransactionsResponse.inflateTransactionInputFromJson(transactionInputJson);
                transaction.addTransactionInput(transactionInput);
            }
        }

        {
            final Json transactionOutputsJson = json.get("vout");
            for (int i = 0; i < transactionOutputsJson.length(); ++i) {
                final Json transactionOutputJson = transactionOutputsJson.get(i);
                final TransactionOutput transactionOutput = GetTransactionsResponse.inflateTransactionOutputFromJson(transactionOutputJson, i);
                transaction.addTransactionOutput(transactionOutput);
            }
        }

        final Sha256Hash transactionHash = transaction.getHash();
        if (! Util.areEqual(expectedTransactionHash, transactionHash)) {
            Logger.info("Unable to inflate Transaction. Expected " + expectedTransactionHash + ", got " + transactionHash + ".");
            return null;
        }

        return transaction;
    }

    public static TransactionData inflateTransactionDataFromJson(final Json transactionObjectJson) {
        final AddressInflater addressInflater = new AddressInflater();

        final HashMap<TransactionOutputIdentifier, Address> previousOutputAddresses = new HashMap<TransactionOutputIdentifier, Address>();
        final HashMap<TransactionOutputIdentifier, Long> previousOutputAmounts = new HashMap<TransactionOutputIdentifier, Long>();

        final Transaction transaction = GetTransactionsResponse.inflateTransactionFromJson(transactionObjectJson);
        if (transaction == null) { return null; }

        final Json transactionInputsJson = transactionObjectJson.get("vin");
        for (int j = 0; j < transactionInputsJson.length(); ++j) {
            final Json transactionInputJson = transactionInputsJson.get(j);

            final Sha256Hash previousTransactionHash = Sha256Hash.fromHexString(transactionInputJson.getString("txid"));
            final Integer previousOutputIndex = transactionInputJson.getInteger("vout");

            final String addressString = transactionInputJson.getString("legacyAddress");
            final Address address = addressInflater.fromBase58Check(addressString);
            if (address == null) { return null; }

            final Long amount = transactionInputJson.getLong("value");

            final TransactionOutputIdentifier transactionOutputIdentifier = new TransactionOutputIdentifier(previousTransactionHash, previousOutputIndex);
            previousOutputAddresses.put(transactionOutputIdentifier, address);
            previousOutputAmounts.put(transactionOutputIdentifier, amount);
        }

        return new TransactionData(transaction, previousOutputAddresses, previousOutputAmounts);
    }

    public static Sha256Hash getTransactionHashFromJson(final Json transactionObjectJson) {
        return Sha256Hash.fromHexString(transactionObjectJson.getString("txid"));
    }

    public GetTransactionsResponse(final HttpResponse httpResponse) {
        super(httpResponse);

        if (this.isSuccess()) {
            final Json json = super.getJson();
            if (! json.isArray()) {
                Logger.warn("Received Invalid Transactions JSON: expected array at top level.");
                _errorMessage = "Invalid response from server.";
                return;
            }

            final Integer itemCount = json.length();
            for (int i = 0; i < itemCount; ++i) {
                final Json transactionObjectJson = json.get(i);
                final TransactionData transactionData = GetTransactionsResponse.inflateTransactionDataFromJson(transactionObjectJson);
                if (transactionData == null) { continue; }

                _transactions.add(transactionData.transaction);
                _previousOutputAddresses.putAll(transactionData.previousOutputAddresses);
                _previousOutputAmounts.putAll(transactionData.previousOutputAmounts);
            }

            _errorMessage = null;
        }
        else {
            final Json jsonResponse = super.getJson();
            _errorMessage = jsonResponse.getOrNull("error", Json.Types.STRING);
        }
    }

    public List<Transaction> getTransactions() {
        return _transactions;
    }

    public Map<TransactionOutputIdentifier, Address> getPreviousOutputAddresses() {
        return _previousOutputAddresses;
    }

    public Map<TransactionOutputIdentifier, Long> getPreviousOutputAmounts() {
        return _previousOutputAmounts;
    }

    @Override
    public String getErrorMessage() {
        return _errorMessage;
    }
}
