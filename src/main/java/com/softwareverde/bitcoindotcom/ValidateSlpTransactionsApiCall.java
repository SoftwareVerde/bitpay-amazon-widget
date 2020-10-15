package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.ApiConfiguration;
import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.HttpResponse;

public class ValidateSlpTransactionsApiCall extends BitcoinDotComApiCall<ValidateSlpTransactionsRequest, ValidateSlpTransactionsResponse> {
    public ValidateSlpTransactionsApiCall(final ApiConfiguration configuration) {
        super(configuration);
    }

    @Override
    public ValidateSlpTransactionsResponse call(final ValidateSlpTransactionsRequest request) throws Exception {
        final String requestPath = "/slp/validateTxid";

        final HttpResponse httpResponse = _call(requestPath, HttpMethod.POST, request);
        return new ValidateSlpTransactionsResponse(httpResponse);
    }
}
