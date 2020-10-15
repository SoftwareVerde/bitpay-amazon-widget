package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.ApiConfiguration;
import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.HttpResponse;

public class GetUtxosForAddressesApiCall extends BitcoinDotComApiCall<GetUtxosForAddressesRequest, GetUtxosForAddressesResponse> {
    public GetUtxosForAddressesApiCall(final ApiConfiguration configuration) {
        super(configuration);
    }

    @Override
    public GetUtxosForAddressesResponse call(final GetUtxosForAddressesRequest request) throws Exception {
        final String requestPath = "/address/utxo";

        final HttpResponse response = _call(requestPath, HttpMethod.POST, request);
        final GetUtxosForAddressesResponse getUtxosForAddressesResponse = new GetUtxosForAddressesResponse(response);
        return getUtxosForAddressesResponse;
    }
}
