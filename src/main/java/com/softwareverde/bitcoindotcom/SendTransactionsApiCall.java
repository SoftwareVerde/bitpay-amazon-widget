package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.ApiConfiguration;
import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.HttpResponse;

public class SendTransactionsApiCall extends BitcoinDotComApiCall<SendTransactionsRequest, SendTransactionsResponse> {
    public SendTransactionsApiCall(final ApiConfiguration configuration) {
        super(configuration);
    }

    @Override
    public SendTransactionsResponse call(final SendTransactionsRequest request) throws Exception {
        final String requestPath = "/rawtransactions/sendRawTransaction";

        final HttpResponse httpResponse = _call(requestPath, HttpMethod.POST, request);
        return new SendTransactionsResponse(httpResponse);
    }
}
