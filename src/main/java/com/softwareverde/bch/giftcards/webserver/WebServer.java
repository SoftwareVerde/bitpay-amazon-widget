package com.softwareverde.bch.giftcards.webserver;

import com.softwareverde.bch.giftcards.configuration.ServerProperties;
import com.softwareverde.http.server.HttpServer;
import com.softwareverde.http.server.endpoint.Endpoint;
import com.softwareverde.http.server.servlet.DirectoryServlet;
import com.softwareverde.http.server.servlet.Servlet;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.util.Util;

import java.io.File;

public class WebServer {
    protected final Environment _environment;
    protected final HttpServer _httpServer;

    protected static Boolean isSslEnabled(final ServerProperties serverProperties) {
        if (serverProperties.getTlsPort() < 1) {
            return false;
        }

        if (Util.isBlank(serverProperties.getTlsCertificateFile())) {
            return false;
        }

        if (Util.isBlank(serverProperties.getTlsKeyFile())) {
            return false;
        }

        return true;
    }

    protected <T extends Servlet> void _assignEndpoint(final String path, final T servlet) {
        final Endpoint endpoint = new Endpoint(servlet);
        endpoint.setStrictPathEnabled(false);
        endpoint.setPath(path);

        _httpServer.addEndpoint(endpoint);
    }

    public WebServer(final Environment environment) {
        _environment = environment;
        _httpServer = new HttpServer();

        final ServerProperties serverProperties = environment.getServerProperties();

        _httpServer.setPort(serverProperties.getPort());

        final boolean sslIsEnabled = WebServer.isSslEnabled(serverProperties);
        { // Configure SSL/TLS...
            if (sslIsEnabled) {
                _httpServer.setTlsPort(serverProperties.getTlsPort());
                _httpServer.setCertificate(serverProperties.getTlsCertificateFile(), serverProperties.getTlsKeyFile());
            }

            _httpServer.enableEncryption(sslIsEnabled);
            _httpServer.redirectToTls(false);
        }

        { // Static Content
            final File servedDirectory = new File(serverProperties.getRootDirectory() +"/");
            final DirectoryServlet indexServlet = new DirectoryServlet(servedDirectory);
            indexServlet.setShouldServeDirectories(true);
            indexServlet.setIndexFile("index.html");
            indexServlet.setErrorHandler(new DirectoryServlet.ErrorHandler() {
                @Override
                public Response onFileNotFound(final Request request) {
                    indexServlet.reIndexFiles();

                    final Response response = new Response();
                    response.setCode(Response.Codes.NOT_FOUND);
                    response.setContent("Not found.");
                    return response;
                }
            });

            final Endpoint endpoint = new Endpoint(indexServlet);
            endpoint.setPath("/");
            endpoint.setStrictPathEnabled(false);
            _httpServer.addEndpoint(endpoint);
        }
    }

    public void start() {
        _httpServer.start();
        System.out.println("[Listening on Port " + _environment.getServerProperties().getPort() + "]");
    }

    public void stop() {
        _httpServer.stop();
    }
}
