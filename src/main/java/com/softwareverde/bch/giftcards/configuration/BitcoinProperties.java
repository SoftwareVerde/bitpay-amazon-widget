package com.softwareverde.bch.giftcards.configuration;

import com.softwareverde.bitcoin.server.configuration.SeedNodeProperties;
import com.softwareverde.constable.list.List;

public class BitcoinProperties {
    protected String _bitcoinRpcUrl;
    protected Integer _bitcoinRpcPort;
    protected List<SeedNodeProperties> _seedNodeProperties;

    public String getBitcoinRpcUrl() { return _bitcoinRpcUrl; }
    public Integer getBitcoinRpcPort() { return _bitcoinRpcPort; }
    public List<SeedNodeProperties> getSeedNodeProperties() { return _seedNodeProperties; }
}
