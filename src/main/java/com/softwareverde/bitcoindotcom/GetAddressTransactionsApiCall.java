package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.ApiConfiguration;
import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.HttpResponse;

public class GetAddressTransactionsApiCall extends BitcoinDotComApiCall<GetAddressTransactionsRequest, GetAddressTransactionsResponse> {
    public GetAddressTransactionsApiCall(final ApiConfiguration configuration) {
        super(configuration);
    }

    @Override
    public GetAddressTransactionsResponse call(final GetAddressTransactionsRequest request) throws Exception {
        final String requestPath = "/address/transactions/" + request.getAddressString();

        final HttpResponse httpResponse = _call(requestPath, HttpMethod.GET, request);
        return new GetAddressTransactionsResponse(httpResponse);
    }
}
