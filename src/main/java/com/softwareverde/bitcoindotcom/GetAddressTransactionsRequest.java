package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.JsonApiRequest;
import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.json.Json;

public class GetAddressTransactionsRequest extends JsonApiRequest {
    protected Address _address = null;

    public void setAddress(final Address address) {
        _address = address;
    }

    public String getAddressString() {
        return _address.toBase58CheckEncoded();
    }

    @Override
    protected Json _toJson() throws Exception {
        return new Json(true);
    }
}
