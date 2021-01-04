package com.softwareverde.bch.giftcards.webserver;

import com.softwareverde.bch.giftcards.configuration.Configuration;
import com.softwareverde.bch.giftcards.configuration.ServerProperties;

public class Environment implements com.softwareverde.http.server.servlet.routed.Environment {
    protected final ServerProperties _serverProperties;

    public Environment(final Configuration configuration) {
        _serverProperties = configuration.getServerProperties();
    }

    public ServerProperties getServerProperties() {
        return _serverProperties;
    }
}
