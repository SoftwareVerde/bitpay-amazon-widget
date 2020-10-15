package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.ApiConfiguration;
import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.HttpResponse;

public class GetTransactionsApiCall extends BitcoinDotComApiCall<GetTransactionsRequest, GetTransactionsResponse> {
    public GetTransactionsApiCall(final ApiConfiguration configuration) {
        super(configuration);
    }

    @Override
    public GetTransactionsResponse call(final GetTransactionsRequest request) throws Exception {
        final String requestPath = "/transaction/details";

        final HttpResponse httpResponse = _call(requestPath, HttpMethod.POST, request);
        return new GetTransactionsResponse(httpResponse);
    }
}
