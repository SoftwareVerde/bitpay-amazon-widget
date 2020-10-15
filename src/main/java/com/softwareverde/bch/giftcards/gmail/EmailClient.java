package com.softwareverde.bch.giftcards.gmail;

import com.softwareverde.bch.giftcards.configuration.ServerProperties;
import com.softwareverde.bch.giftcards.webserver.Environment;
import com.softwareverde.constable.list.List;
import com.softwareverde.logging.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailClient {
    protected final Environment _environment;

    public void email(final String subject, final String content) {
        final ServerProperties serverProperties = _environment.getServerProperties();

        final Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        final Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                final String username = serverProperties.getEmailUsername();
                final String password = serverProperties.getEmailPassword();

                return new PasswordAuthentication(username, password);
            }
        });

        try {
            final List<String> recipientEmailAddresses = serverProperties.getEmailRecipients();
            final Address[] recipientAddresses = new Address[recipientEmailAddresses.getCount()];
            for (int i = 0; i < recipientEmailAddresses.getCount(); ++i) {
                final String recipientEmailAddress = recipientEmailAddresses.get(i);
                final Address[] addresses = InternetAddress.parse(recipientEmailAddress);
                recipientAddresses[i] = addresses[0];
            }
            final String fromEmailUsername = serverProperties.getEmailUsername();

            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmailUsername));
            message.setRecipients(Message.RecipientType.TO, recipientAddresses);
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
        }
        catch (final Exception exception) {
            Logger.warn(exception);
        }
    }

    public EmailClient(final Environment environment) {
        _environment = environment;
    }
}
