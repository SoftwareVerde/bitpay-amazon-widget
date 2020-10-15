package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.JsonApiRequest;
import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.json.Json;

import java.util.ArrayList;
import java.util.List;

public class GetUtxosForAddressesRequest extends JsonApiRequest {
    protected List<Address> _addresses = new ArrayList<>();

    public List<Address> getAddresses() {
        return _addresses;
    }

    public void addAddress(final Address address) {
        _addresses.add(address);
    }

    public void setAddresses(final List<Address> addresses) {
        _addresses = addresses;
    }

    @Override
    protected Json _toJson() throws Exception {
        final Json addressesArray = new Json(true);
        for (final Address address : _addresses) {
            final String addressString = address.toBase32CheckEncoded();
            addressesArray.add(addressString);
        }

        final Json json = new Json();
        json.put("addresses", addressesArray);
        return json;
    }
}
