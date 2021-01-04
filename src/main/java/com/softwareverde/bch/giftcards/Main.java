package com.softwareverde.bch.giftcards;

import com.softwareverde.bch.giftcards.configuration.Configuration;
import com.softwareverde.bch.giftcards.webserver.Environment;
import com.softwareverde.bch.giftcards.webserver.WebServer;
import com.softwareverde.bitcoin.CoreInflater;
import com.softwareverde.bitcoin.inflater.MasterInflater;
import com.softwareverde.logging.LogLevel;
import com.softwareverde.logging.Logger;
import com.softwareverde.logging.slf4j.Slf4jLogFactory;

import java.io.File;

public class Main {
    protected static void exitFailure() {
        System.exit(1);
    }

    protected static void exitSuccess() {
        System.exit(0);
    }

    protected static void printError(final String errorMessage) {
        System.err.println(errorMessage);
    }

    protected static void printUsage() {
        Main.printError("Usage: java -jar " + System.getProperty("java.class.path") + " <configuration-file>");
    }

    protected static Configuration loadConfigurationFile(final String configurationFilename, final MasterInflater masterInflater) {
        final File configurationFile =  new File(configurationFilename);
        if (! configurationFile.isFile()) {
            Main.printError("Invalid configuration file: " + configurationFilename);
            Main.exitFailure();
        }

        return new Configuration(configurationFile, masterInflater);
    }

    public static void main(final String[] commandLineArguments) {
        Logger.setLogFactory(new Slf4jLogFactory());
        Logger.setLogLevel(LogLevel.DEBUG);

        if (commandLineArguments.length != 1) {
            Main.printUsage();
            Main.exitFailure();
        }

        final MasterInflater masterInflater = new CoreInflater();
        final String configurationFilename = commandLineArguments[0];
        final Configuration configuration = Main.loadConfigurationFile(configurationFilename, masterInflater);

        final Environment environment = new Environment(configuration);
        final WebServer webServer = new WebServer(environment);

        Logger.info("[Starting Web Server]");
        webServer.start();

        while (true) {
            try { Thread.sleep(1000); } catch (final Exception exception) { break; }
        }

        Logger.info("[Shutting Down]");
        webServer.stop();
    }
}
