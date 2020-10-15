package com.softwareverde.bch.giftcards.webserver.api.v1.get;

import com.softwareverde.bch.giftcards.webserver.Environment;
import com.softwareverde.bch.giftcards.webserver.api.endpoint.ExchangeRateApi;
import com.softwareverde.bitcoin.PriceIndexer;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.JsonResponse;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.RequestHandler;

import java.util.Map;

public class BitcoinCashExchangeRateHandler implements RequestHandler<Environment> {
    protected final PriceIndexer _priceIndexer;

    public BitcoinCashExchangeRateHandler(final PriceIndexer priceIndexer) {
        _priceIndexer = priceIndexer;
    }

    @Override
    public Response handleRequest(final Request request, final Environment environment, final Map<String, String> parameters) throws Exception {
        final PriceIndexer priceIndexer = environment.getPriceIndexer();

        final ExchangeRateApi.GetExchangeRateResult devTokenExchangeRateResult = new ExchangeRateApi.GetExchangeRateResult();

        final Double dollarsPerBitcoin = priceIndexer.getDollarsPerBitcoin();
        devTokenExchangeRateResult.setDollarsPerBitcoin(dollarsPerBitcoin);

        devTokenExchangeRateResult.setWasSuccess(true);
        return new JsonResponse(Response.Codes.OK, devTokenExchangeRateResult);
    }
}
