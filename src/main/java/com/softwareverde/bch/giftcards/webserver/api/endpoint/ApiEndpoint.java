package com.softwareverde.bch.giftcards.webserver.api.endpoint;

import com.softwareverde.bch.giftcards.webserver.Environment;
import com.softwareverde.http.server.servlet.routed.json.JsonApplicationServlet;

public abstract class ApiEndpoint extends JsonApplicationServlet<Environment> {
    public ApiEndpoint(final Environment environment) {
        super(environment);
    }
}