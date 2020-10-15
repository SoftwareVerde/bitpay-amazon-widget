package com.softwareverde.bch.giftcards.configuration;

import com.softwareverde.util.Util;

import java.util.Properties;

public class BitcoinPropertiesLoader {
    public BitcoinPropertiesLoader() { }

    public BitcoinProperties loadBitcoinProperties(final Properties properties) {
        final BitcoinProperties bitcoinProperties = new BitcoinProperties();

        bitcoinProperties._bitcoinRpcUrl = properties.getProperty("bitcoin.rpcUrl", "localhost");
        bitcoinProperties._bitcoinRpcPort = Util.parseInt(properties.getProperty("bitcoin.rpcPort", "8334"));

        final String defaultSeedNodes = "[\"btc.softwareverde.com\", \"bitcoinverde.org\", \"bchd.greyh.at\"]";
        bitcoinProperties._seedNodeProperties = PropertiesUtil.parseSeedNodeProperties("bitcoin.seedNodes", 8333, defaultSeedNodes, properties);

        return bitcoinProperties;
    }
}
