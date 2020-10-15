package com.softwareverde.bitcoindotcom;

import com.softwareverde.bitcoin.PriceIndexer;
import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.HttpRequest;
import com.softwareverde.http.HttpResponse;
import com.softwareverde.http.WebRequest;
import com.softwareverde.json.Json;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.Container;

public class BitcoinDotComPriceIndexer implements PriceIndexer {
    @Override
    public Double getDollarsPerBitcoin() {
        final WebRequest webRequest = new WebRequest();
        webRequest.setUrl("https://index-api.bitcoin.com/api/v0/cash/price/usd");
        webRequest.setHeader("user-agent", ""); // Necessary for OpenJdk v1.8 on Linux.
        webRequest.setMethod(HttpMethod.GET);

        final Container<HttpResponse> httpResponse = new Container<HttpResponse>();
        webRequest.execute(new HttpRequest.Callback() {
            @Override
            public void run(final HttpResponse response) {
                synchronized (httpResponse) {
                    httpResponse.value = response;
                    httpResponse.notifyAll();
                }
            }
        });

        synchronized (httpResponse) {
            try {
                httpResponse.wait(5000L);

                final Json result = httpResponse.value.getJsonResult();
                final Long price = result.getLong("price");
                if (price <= 0L) { throw new Exception(); }

                return (price / 100D);
            }
            catch (final Exception exception) {
                Logger.warn("Unable to load price index.");
                return null;
            }
        }
    }
}
