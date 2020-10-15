package com.softwareverde.bch.giftcards.webserver;

import com.softwareverde.bitcoin.address.Address;

public interface NewAddressCreatedCallback {
    void newAddressCreated(Address address);
}