package com.softwareverde.bch.giftcards.configuration;

import com.softwareverde.bitcoin.server.configuration.SeedNodeProperties;
import com.softwareverde.constable.list.List;
import com.softwareverde.constable.list.mutable.MutableList;
import com.softwareverde.json.Json;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.Util;

import java.util.Properties;

public class PropertiesUtil {
    public static String[] parseStringArrayProperty(final String propertyName, final String defaultValue, final Properties properties) {
        final String jsonString;
        {
            final String inputString = properties.getProperty(propertyName, defaultValue)
                .replaceAll("^[\\s\\[]*", "")
                .replaceAll("[\\s\\[]*$", "");

            jsonString = "[" + inputString + "]";
        }

        if (! Json.isJson(jsonString)) {
            Logger.warn("Invalid property value for " + propertyName + ": " + jsonString);
            return new String[0];
        }

        final Json stringArrayJson = Json.parse(jsonString);
        if (! stringArrayJson.isArray()) { return new String[0]; }

        final int itemCount = stringArrayJson.length();
        final String[] strings = new String[itemCount];
        for (int i = 0; i < itemCount; ++i) {
            final String string = stringArrayJson.getString(i);
            strings[i] = string;
        }
        return strings;
    }

    // TODO: Use PropertiesUtil::parseStringArrayProperty ??
    public static List<SeedNodeProperties> parseSeedNodeProperties(final String propertyName, final Integer defaultNetworkPort, final String defaultValue, final Properties properties) {
        final String propertyStringValue = properties.getProperty(propertyName, defaultValue);
        if (propertyStringValue == null) { return null; }

        final Json seedNodesJson = Json.parse(propertyStringValue);
        final MutableList<SeedNodeProperties> nodePropertiesList = new MutableList<SeedNodeProperties>(seedNodesJson.length());
        for (int i = 0; i < seedNodesJson.length(); ++i) {
            final String propertiesString = seedNodesJson.getString(i);

            final SeedNodeProperties nodeProperties;
            final int indexOfColon = propertiesString.indexOf(":");
            if (indexOfColon < 0) {
                nodeProperties = new SeedNodeProperties(propertiesString, defaultNetworkPort);
            }
            else {
                final String address = propertiesString.substring(0, indexOfColon);
                final Integer port = Util.parseInt(propertiesString.substring(indexOfColon + 1));
                nodeProperties = new SeedNodeProperties(address, port);
            }

            nodePropertiesList.add(nodeProperties);
        }
        return nodePropertiesList;
    }

    protected PropertiesUtil() { }
}
