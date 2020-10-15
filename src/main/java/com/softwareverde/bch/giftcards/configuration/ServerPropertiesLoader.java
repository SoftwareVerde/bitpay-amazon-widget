package com.softwareverde.bch.giftcards.configuration;

import com.softwareverde.bitcoin.address.AddressInflater;
import com.softwareverde.bitcoin.inflater.MasterInflater;
import com.softwareverde.bitcoin.secp256k1.privatekey.PrivateKeyInflater;
import com.softwareverde.bitcoin.slp.SlpTokenId;
import com.softwareverde.cryptography.secp256k1.key.PrivateKey;
import com.softwareverde.util.Util;

import java.util.Properties;

public class ServerPropertiesLoader {
    protected final MasterInflater _masterInflater;

    public ServerPropertiesLoader(final MasterInflater masterInflater) {
        _masterInflater = masterInflater;
    }

    public ServerProperties loadServerProperties(final Properties properties) {
        final PrivateKeyInflater privateKeyInflater = new PrivateKeyInflater();
        final AddressInflater addressInflater = _masterInflater.getAddressInflater();

        final ServerProperties serverProperties = new ServerProperties();

        serverProperties._rootDirectory = properties.getProperty("server.rootDirectory", "");
        serverProperties._port = Util.parseInt(properties.getProperty("server.httpPort", "80"));
        serverProperties._tlsPort = Util.parseInt(properties.getProperty("server.tlsPort", "443"));
        serverProperties._socketPort = Util.parseInt(properties.getProperty("server.socketPort", "444"));
        serverProperties._tlsCertificateFile = properties.getProperty("server.tlsCertificateFile", "");
        serverProperties._tlsKeyFile = properties.getProperty("server.tlsKeyFile", "");

        final String destinationAddressString = properties.getProperty("server.destinationAddress", "");
        serverProperties._destinationAddress = Util.coalesce(addressInflater.fromBase58Check(destinationAddressString), addressInflater.fromBase32Check(destinationAddressString));

        serverProperties._useUniqueDonationAddresses = Util.parseBool(properties.getProperty("server.useUniqueDonationAddresses", "1"));

        serverProperties._emailUsername = properties.getProperty("server.emailUsername", null);
        serverProperties._emailPassword = properties.getProperty("server.emailPassword", null);

        return serverProperties;
    }
}
