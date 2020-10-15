package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.JsonApiResponse;
import com.softwareverde.http.HttpResponse;
import com.softwareverde.json.Json;
import com.softwareverde.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class GetUtxosForAddressesResponse extends JsonApiResponse {
    protected List<AddressUtxos> _addressUtxosList;
    protected String _errorMessage;

    public GetUtxosForAddressesResponse(final HttpResponse httpResponse) {
        super(httpResponse);

        if (isSuccess()) {
            final Json responseJson = super.getJson();
            if (! responseJson.isArray()) {
                Logger.warn("Invalid Address UTXO List JSON: expected top-level array.");
                return;
            }
            final List<AddressUtxos> addressUtxosList = new ArrayList<>();
            for (int i = 0; i < responseJson.length(); i++) {
                final Json addressUtxosJson = responseJson.getOrNull(i, Json.Types.OBJECT);
                final AddressUtxos addressUtxos = AddressUtxos.fromJson(addressUtxosJson);
                addressUtxosList.add(addressUtxos);
            }
            _addressUtxosList = addressUtxosList;
        }
        else {
            final Json jsonResponse = super.getJson();
            _errorMessage = jsonResponse.getOrNull("error", Json.Types.STRING);
        }
    }

    public List<AddressUtxos> getAddressUtxosList() {
        return _addressUtxosList;
    }

    @Override
    public String getErrorMessage() {
        return _errorMessage;
    }
}
