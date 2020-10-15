package com.softwareverde.bch.giftcards.webserver.api.endpoint;

import com.softwareverde.bch.giftcards.webserver.Environment;
import com.softwareverde.bch.giftcards.webserver.NewAddressCreatedCallback;
import com.softwareverde.bch.giftcards.webserver.api.ApiResult;
import com.softwareverde.bch.giftcards.webserver.api.v1.post.NewRedemptionAddressHandler;
import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.http.HttpMethod;
import com.softwareverde.json.Json;

public class RedeemApi extends ApiEndpoint {
    public static class NewRedemptionAddressResult extends ApiResult {
        private Address _address;

        public void setAddress(final Address address) {
            _address = address;
        }

        @Override
        public Json toJson() {
            final Json json = super.toJson();

            final Json addressJson;
            {
                addressJson = new Json(false);
                addressJson.put("base58CheckEncoded", (_address != null ? _address.toBase58CheckEncoded() : null));
                addressJson.put("base32CheckEncoded", (_address != null ? _address.toBase32CheckEncoded(true) : null));
            }
            json.put("address", addressJson);

            return json;
        }
    }

    protected NewAddressCreatedCallback _newAddressCreatedCallback;

    public RedeemApi(final String apiPrePath, final Environment environment) {
        super(environment);

        _defineEndpoint((apiPrePath + "/redeem/new"), HttpMethod.POST, new NewRedemptionAddressHandler(new NewAddressCreatedCallback() {
            @Override
            public void newAddressCreated(final Address address) {
                final NewAddressCreatedCallback newAddressCreatedCallback = _newAddressCreatedCallback;
                if (newAddressCreatedCallback != null) {
                    newAddressCreatedCallback.newAddressCreated(address);
                }
            }
        }));
    }

    public void setNewAddressCreatedCallback(final NewAddressCreatedCallback newAddressCreatedCallback) {
        _newAddressCreatedCallback = newAddressCreatedCallback;
    }
}
