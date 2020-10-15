package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.JsonApiResponse;
import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.address.AddressInflater;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.bitcoin.transaction.TransactionInflater;
import com.softwareverde.bitcoin.transaction.output.identifier.TransactionOutputIdentifier;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.http.HttpResponse;
import com.softwareverde.json.Json;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.HexUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetRawTransactionsResponse extends JsonApiResponse {
    protected List<Transaction> _transactions = new ArrayList<>();
    protected Map<TransactionOutputIdentifier, Address> _previousOutputAddresses = new HashMap<>();
    protected Map<TransactionOutputIdentifier, Long> _previousOutputAmounts = new HashMap<>();
    protected String _errorMessage;

    public GetRawTransactionsResponse(final HttpResponse httpResponse) {
        super(httpResponse);

        if (isSuccess()) {
            final Json jsonResponse = super.getJson();
            if (! jsonResponse.isArray()) {
                Logger.warn("Received Invalid Transactions JSON: expected array at top level.");
                return;
            }

            {
                final TransactionInflater transactionInflater = new TransactionInflater();
                final List<Transaction> transactions = new ArrayList<>();
                for (int i = 0; i < jsonResponse.length(); i++) {
                    final String transactionHex = jsonResponse.getOrNull(i, Json.Types.STRING);
                    if (transactionHex == null) {
                        Logger.warn("Received Invalid Transactions JSON: null transaction object in position " + i);
                        return;
                    }
                    final byte[] transactionBytes = HexUtil.hexStringToByteArray(transactionHex);
                    final Transaction transaction = transactionInflater.fromBytes(transactionBytes);
                    transactions.add(transaction);
                }
                _transactions = transactions;
            }

            {
                final AddressInflater addressInflater = new AddressInflater();
                final HashMap<TransactionOutputIdentifier, Address> previousOutputAddresses = new HashMap<TransactionOutputIdentifier, Address>();
                final HashMap<TransactionOutputIdentifier, Long> previousOutputAmounts = new HashMap<TransactionOutputIdentifier, Long>();

                final Integer itemCount = jsonResponse.length();
                for (int i = 0; i < itemCount; ++i) {
                    final Json transactionObjectJson = jsonResponse.get(i);
                    final Json transactionInputsJson = transactionObjectJson.get("vin");
                    for (int j = 0; j < transactionInputsJson.length(); ++j) {
                        final Json transactionInputJson = transactionInputsJson.get(j);

                        final Sha256Hash previousTransactionHash = Sha256Hash.fromHexString(transactionInputJson.getString("txid"));
                        final Integer previousOutputIndex = transactionInputJson.getInteger("vout");

                        final Json addressesJson = transactionInputJson.get("addresses");
                        if (addressesJson.length() > 0) {

                            final String addressString = addressesJson.getString(0);
                            final Address address = addressInflater.fromBase58Check(addressString);

                            final TransactionOutputIdentifier transactionOutputIdentifier = new TransactionOutputIdentifier(previousTransactionHash, previousOutputIndex);
                            previousOutputAddresses.put(transactionOutputIdentifier, address);

                            final Long amount = transactionInputJson.getLong("value");
                            previousOutputAmounts.put(transactionOutputIdentifier, amount);
                        }
                    }
                }

                _previousOutputAddresses = previousOutputAddresses;
                _previousOutputAmounts = previousOutputAmounts;
            }
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
