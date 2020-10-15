package com.softwareverde.bch.giftcards.webserver.api.endpoint;

import com.softwareverde.bch.giftcards.webserver.Environment;
import com.softwareverde.bch.giftcards.webserver.api.ApiResult;
import com.softwareverde.bch.giftcards.webserver.api.v1.get.BitcoinCashExchangeRateHandler;
import com.softwareverde.bitcoin.PriceIndexer;
import com.softwareverde.http.HttpMethod;
import com.softwareverde.json.Json;

public class ExchangeRateApi extends ApiEndpoint {
    public static class GetExchangeRateResult extends ApiResult {
        private Double _dollarsPerBitcoin;

        public void setDollarsPerBitcoin(final Double dollarsPerBitcoin) {
            _dollarsPerBitcoin = dollarsPerBitcoin;
        }

        @Override
        public Json toJson() {
            final Json json = super.toJson();
            json.put("dollarsPerBitcoin", _dollarsPerBitcoin);
            return json;
        }
    }

    public ExchangeRateApi(final String apiPrePath, final Environment environment) {
        super(environment);

        final PriceIndexer priceIndexer = environment.getPriceIndexer();
        _defineEndpoint((apiPrePath + "/exchange-rate"), HttpMethod.GET, new BitcoinCashExchangeRateHandler(priceIndexer));
    }
}
