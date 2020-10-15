package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.JsonApiResponse;
import com.softwareverde.http.HttpResponse;
import com.softwareverde.json.Json;

public class SendTransactionsResponse extends JsonApiResponse {
    private String _errorMessage;

    public SendTransactionsResponse(final HttpResponse httpResponse) {
        super(httpResponse);

        if (isSuccess()) {
            // nothing to parse
        }
        else {
            final Json jsonResponse = super.getJson();
            _errorMessage = jsonResponse.getOrNull("error", Json.Types.STRING);
        }
    }

    @Override
    public String getErrorMessage() {
        return _errorMessage;
    }
}
