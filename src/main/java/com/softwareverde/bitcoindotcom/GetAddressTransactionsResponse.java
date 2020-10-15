package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.JsonApiResponse;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.http.HttpResponse;
import com.softwareverde.json.Json;

import java.util.ArrayList;
import java.util.List;

public class GetAddressTransactionsResponse extends JsonApiResponse {
    protected List<Sha256Hash> _transactionHashes = new ArrayList<>();
    protected String _errorMessage;

    public GetAddressTransactionsResponse(final HttpResponse httpResponse) {
        super(httpResponse);

        if (isSuccess()) {
            final Json responseJson = httpResponse.getJsonResult();
            final Json transactionObjectsJson = responseJson.get("txs");

            final int itemCount = transactionObjectsJson.length();
            final List<Sha256Hash> transactionHashes = new ArrayList<Sha256Hash>(itemCount);
            for (int i = 0; i < itemCount; ++i) {
                final Json transactionObjectJson = transactionObjectsJson.get(i);
                final String transactionHashString = transactionObjectJson.getString("txid");
                final Sha256Hash transactionHash = Sha256Hash.fromHexString(transactionHashString);
                transactionHashes.add(transactionHash);
            }

            _transactionHashes = transactionHashes;
        }
        else {
            final Json jsonResponse = super.getJson();
            _errorMessage = jsonResponse.getOrNull("error", Json.Types.STRING);

        }
    }

    public List<Sha256Hash> getTransactionHashes() {
        return _transactionHashes;
    }

    @Override
    public String getErrorMessage() {
        return _errorMessage;
    }
}
