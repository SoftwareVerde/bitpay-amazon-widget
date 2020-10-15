package com.softwareverde.bch.giftcards.configuration;

import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.constable.list.List;

public class ServerProperties {
    protected String _rootDirectory;
    protected String _tlsCertificateFile;
    protected String _tlsKeyFile;
    protected Integer _port;
    protected Integer _tlsPort;
    protected Integer _socketPort;

    protected Address _destinationAddress;
    protected Boolean _useUniqueDonationAddresses;

    protected String _emailUsername;
    protected String _emailPassword;
    protected List<String> _emailRecipients;

    public String getRootDirectory() { return _rootDirectory; }
    public String getTlsCertificateFile() { return _tlsCertificateFile; }
    public String getTlsKeyFile() { return _tlsKeyFile; }
    public Integer getPort() { return _port; }
    public Integer getTlsPort() { return _tlsPort; }
    public Integer getSocketPort() { return _socketPort; }

    public Address getDestinationAddress() { return _destinationAddress; }
    public Boolean useUniqueDonationAddresses() { return _useUniqueDonationAddresses; }

    public String getEmailUsername() { return _emailUsername; }
    public String getEmailPassword() { return _emailPassword; }
    public List<String> getEmailRecipients() { return _emailRecipients; }
}

