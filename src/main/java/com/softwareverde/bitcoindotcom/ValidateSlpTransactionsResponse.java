package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.JsonApiResponse;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.http.HttpResponse;
import com.softwareverde.json.Json;
import com.softwareverde.logging.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ValidateSlpTransactionsResponse extends JsonApiResponse {
    private Map<Sha256Hash, Boolean> _slpValidity = new HashMap<>();
    private Map<Sha256Hash, String> _invalidityReasons = new HashMap<>();
    private String _errorMessage;

    public ValidateSlpTransactionsResponse(final HttpResponse httpResponse) {
        super(httpResponse);

        if (isSuccess()) {
            final Json jsonResponse = super.getJson();
            if (! jsonResponse.isArray()) {
                Logger.warn("Received Invalid SLP Validation JSON: expected array at top level.");
                return;
            }
            final Map<Sha256Hash, Boolean> slpValidity = new HashMap<>();
            for (int i=0; i<jsonResponse.length(); i++) {
                final Json validityObject = jsonResponse.getOrNull(i, Json.Types.JSON);
                if (validityObject == null) {
                    Logger.warn("Unable to parse validity object in position " + i + " of SLP validation results.");
                    continue;
                }

                final String transactionHashString = validityObject.getOrNull("txid", Json.Types.STRING);
                final Boolean isValid = validityObject.getOrNull("valid", Json.Types.BOOLEAN);

                if (transactionHashString == null) {
                    Logger.warn("Missing \"txid\" field at position " + i + " in SLP validation results.");
                    continue;
                }
                final Sha256Hash transactionHash = Sha256Hash.fromHexString(transactionHashString);
                if (transactionHash == null) {
                    Logger.warn("Invalid \"txid\" field at position " + i + " in SLP validation results: " + transactionHashString);
                    continue;
                }
                if (isValid == null) {
                    Logger.warn("Missing \"valid\" field at position " + i + " in SLP validation results (tx: " + transactionHash.toString() + ").");
                    continue;
                }

                slpValidity.put(transactionHash, isValid);
                if (! isValid) {
                    final String invalidityReason = validityObject.getOrNull("invalidReason", Json.Types.STRING);
                    _invalidityReasons.put(transactionHash, invalidityReason);

                    Logger.debug(transactionHash.toString() + " invalidity reason: " + invalidityReason);
                }
            }
            _slpValidity = slpValidity;
        }
        else {
            final Json jsonResponse = super.getJson();
            _errorMessage = jsonResponse.getOrNull("error", Json.Types.STRING);
        }
    }

    public Boolean isValid(final String transactionHash) {
        return _slpValidity.get(Sha256Hash.fromHexString(transactionHash));
    }

    public Boolean isValid(final Sha256Hash transactionHash) {
        return _slpValidity.get(transactionHash);
    }

    public Set<Sha256Hash> getTransactions() {
        return new HashSet<>(_slpValidity.keySet());
    }

    public Map<Sha256Hash, Boolean> getSlpValidity() {
        return new HashMap<>(_slpValidity);
    }

    public String getInvalidityReason(final Sha256Hash transactionHash) {
        return _invalidityReasons.get(transactionHash);
    }

    @Override
    public String getErrorMessage() {
        return _errorMessage;
    }
}
