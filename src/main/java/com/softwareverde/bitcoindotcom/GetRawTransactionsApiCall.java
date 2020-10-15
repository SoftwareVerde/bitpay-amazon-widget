package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.ApiConfiguration;
import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.HttpResponse;

public class GetRawTransactionsApiCall extends BitcoinDotComApiCall<GetRawTransactionsRequest, GetRawTransactionsResponse> {
    public GetRawTransactionsApiCall(final ApiConfiguration configuration) {
        super(configuration);
    }

    @Override
    public GetRawTransactionsResponse call(final GetRawTransactionsRequest request) throws Exception {
        final String requestPath = "/rawtransactions/getRawTransaction";

        final HttpResponse httpResponse = _call(requestPath, HttpMethod.POST, request);
        return new GetRawTransactionsResponse(httpResponse);
    }
}
