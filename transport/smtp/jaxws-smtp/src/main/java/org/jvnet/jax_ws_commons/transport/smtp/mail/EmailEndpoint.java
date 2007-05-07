package org.jvnet.jax_ws_commons.transport.smtp.mail;

import org.jvnet.jax_ws_commons.transport.smtp.SMTPFeature;
import org.jvnet.jax_ws_commons.transport.smtp.POP3Info;
import org.jvnet.jax_ws_commons.transport.smtp.SenderInfo;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Vivek Pandey
 */
public class EmailEndpoint {

    /**
     * The from that receives replies i.e. From
     */
    private final InternetAddress from;

    private final Listener listener;

    /**
     * The JavaMail configuration.
     */
    private final Session session;

    private final SenderThread sender;

    private MailHandler handler;

    private boolean log;

    /**
     * Logger for event logging.
     */
    protected final Logger logger = Logger.getLogger(getClass().getName());


    public EmailEndpoint(String endpointURL) {
        // split into the SMTP part and listener part
        int idx = endpointURL.indexOf('!');

        try {
            if (idx < 0)
                throw new ParseException("the smtp protocol string needs to contain '!'", -1);

            URI smtp = new URI(endpointURL.substring(0, idx));
            String listener = endpointURL.substring(idx + 1);

            Listener listenerObject;

            if (listener.startsWith("pop3://") || listener.startsWith("pop3s://"))
                listenerObject = createPop3Listener(listener, idx + 1);
//            else
//            if(listener.startsWith("imap4://"))
//                listenerObject = createImap4Listener(listener,idx+1);
//            else
//            if(listener.startsWith("maildir://"))
//                listenerObject = createMailDirListener(listener,idx+1);
//            else
//            if(listener.startsWith("tcp://"))
//                listenerObject = createTcpListener(listener,idx+1);
            else
                throw new ParseException("Unsupported scheme: " + listener, idx + 1);

            if (smtp.getUserInfo() == null)
                throw new ParseException("user name is missing", -1);

            UrlQueryParser smtpQuery = new UrlQueryParser(smtp);

            Properties props = new Properties(System.getProperties());
            smtpQuery.addTo(props);

            // translate known query parameters
            if (smtpQuery.getValue("host") != null)
                props.put("mail.smtp.host", smtpQuery.getValue("host"));




            this.listener = listenerObject;
            String user;
            String auth = props.getProperty("mail.smtp.auth");
            if(auth != null && auth.equalsIgnoreCase("true")){
                String userInfo = smtp.getUserInfo();
                idx = userInfo.indexOf(':');
                if (idx < 0)
                    throw new ParseException("Authentication is required password is needed", idx);
                user = userInfo.substring(0, idx);
                this.session = Session.getInstance(props, new SMTPAuthenticator(user, userInfo.substring(idx+1, userInfo.length())));
            }else{
                this.session = Session.getInstance(props);
                user = smtp.getUserInfo();
            }

            this.from = new InternetAddress(
                    user + '@' + smtp.getHost(),
                    smtpQuery.getValue("personal"));


            this.sender = new SenderThread(session);
            if (session == null)
                throw new IllegalArgumentException();
            this.listener.setEndPoint(this);
        } catch (URISyntaxException e) {
            throw new WebServiceException(new ParseException(e.getMessage(), e.getIndex()));
        } catch (UnsupportedEncodingException e) {
            throw new WebServiceException("Unsupported encoding: " + e.getMessage());
        } catch (ParseException e) {
            throw new WebServiceException(e);
        }
    }

    public EmailEndpoint(SMTPFeature feature) {
        SenderInfo senderInfo = feature.getOutgoing();
        Properties props = new Properties(System.getProperties());
        if (props.getProperty("mail.smtp.host") == null) {
            props.put("mail.smtp.host", senderInfo.getHost());
        }
        if (props.getProperty("mail.smtp.port") == null && senderInfo.getPort() != null) {
            props.put("mail.smtp.port", senderInfo.getPort());
        }
        try {
            this.from = new InternetAddress(senderInfo.getFrom(), null);
        } catch (UnsupportedEncodingException e) {
            throw new WebServiceException("Unsupported encoding: " + e.getMessage());
        }
        this.session = Session.getInstance(props);
        this.sender = new SenderThread(session);

        POP3Info pop3 = feature.getIncoming();
        this.listener = new POP3Listener(pop3.getScheme(), pop3.getHost(), pop3.getPort(),
                pop3.getUid(), pop3.getPassword(), pop3.getInterval());
        this.listener.setEndPoint(this);


    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {

        private final String user;
        private final String password;

        public SMTPAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, password);
		}
	}
    /**
     * Creates a new e-mail end point.
     * <p/>
     * <p/>
     * This uses {@code Session.getInstance(System.getProperties())} as the JavaMail session,
     * so effectively it configures JavaMail from the system properties.
     *
     * @param address  The e-mail from of this endpoint.
     * @param listener The object that fetches incoming e-mails.
     */
    public EmailEndpoint(InternetAddress address, Listener listener) {
        this(address, listener, Session.getInstance(System.getProperties()));
    }

    /**
     * Creates a new e-mail end point.
     * <p/>
     * <p/>
     * This version takes the from as string so that it can be invoked from Spring.
     * It's just a short-cut for:
     * <pre>
     * this(name,new InternetAddress(from),listener,Session.getInstance(System.getProperties()))
     * </pre>
     *
     * @see #EmailEndpoint(InternetAddress,Listener)
     */
    public EmailEndpoint(String address, Listener listener) throws AddressException {
        this(new InternetAddress(address), listener, Session.getInstance(System.getProperties()));
    }

    /**
     * Creates a new e-mail end point.
     *
     * @param address  The e-mail from of this endpoint.
     * @param listener The object that fetches incoming e-mails.
     * @param session  The JavaMail configuration.
     */
    public EmailEndpoint(InternetAddress address, Listener listener, Session session) {
        this.from = address;
        this.listener = listener;
        this.session = session;
        this.sender = new SenderThread(session);
        if (address == null || listener == null || session == null)
            throw new IllegalArgumentException();
        listener.setEndPoint(this);
    }

    public void start() {
        listener.start();
        sender.start();
    }

    public void stop() {
        listener.stop();
        sender.shutDown();
    }

    /**
     * Sets the handler that receives uncorrelated incoming e-mails.
     *
     * @see MailHandler
     */
    public void setMailHandler(MailHandler handler) {
        this.handler = handler;
    }

    /**
     * Gets the JavaMail session that this endpoint uses to configure
     * JavaMail.
     *
     * @return always non-null.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the e-mail from that this endpoint is connected to.
     */
    public InternetAddress getFrom() {
        return from;
    }

    public static UUID getKey(MimeMessage msg) {
        try {
            UUID id = getIdHeader(msg, "References");
            if (id != null) return id;

            return getIdHeader(msg, "In-reply-to");
        } catch (MessagingException e) {
            throw new EmailException(e);
        }
    }


    protected void onNewMessage(MimeMessage msg) {
        if (handler != null) {
            try {
                handler.onNewMail(msg);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Unhandled exception", e);
            }
        }
    }

    private static UUID getIdHeader(Message msg, String name) throws MessagingException {
        String[] h = msg.getHeader(name);
        if (h == null || h.length == 0)
            return null;

        String val = h[0].trim();
        if (val.length() < 2) return null;

        // find the last token, if there are more than one
        int idx = val.lastIndexOf(' ');
        if (idx > 0) val = val.substring(idx + 1);

        if (!val.startsWith("<")) return null;
        val = val.substring(1);
        if (!val.endsWith("@localhost>")) return null;
        val = val.substring(0, val.length() - "@localhost>".length());

        try {
            return UUID.fromString(val);
        } catch (IllegalArgumentException e) {
            return null;    // not a UUID
        }
    }

    protected void handleMessage(MimeMessage msg) {
        if (log)
            dump(msg, "Response Message");
        onNewMessage(msg);
    }

    /**
     * Sends a message and return immediately.
     * <p>
     * Use this method when no further reply is expected.
     */
    public UUID send(MimeMessage msg) {
        try {
            String[] rt = msg.getHeader("Reply-To");
            if (rt == null || rt.length == 0) {
                msg.setReplyTo(new Address[]{from});
            }
            if (msg.getFrom() == null || msg.getFrom().length == 0) {
                msg.setFrom(from);
                // msg.setFrom(new InternetAddress("kk@kohsuke.org"));
            }
            if (msg.getRecipients(Message.RecipientType.TO) == null || msg.getRecipients(Message.RecipientType.TO).length == 0) {
                msg.setRecipient(Message.RecipientType.TO, from);
            }

            // this creates a cryptographically strong GUID,
            // meaning someone who knows any number of GUIDs can't
            // predict another one (to steal the session)
            UUID uuid = UUID.randomUUID();
            msg.setHeader("Message-ID", '<' + uuid.toString() + "@localhost>");

            if (log)
                dump(msg, "Request Message");
            sender.queue(msg);

            return uuid;
        } catch (MessagingException e) {
            throw new EmailException(e);
        }
    }

    public void enableLog() {
        this.log = true;
    }

    private Listener createPop3Listener(String listener, int startIndex) throws URISyntaxException, ParseException {
        try {
            URI uri = new URI(listener);
            UrlQueryParser query = new UrlQueryParser(uri);

            String userInfo = uri.getUserInfo();
            if (userInfo == null)
                throw new ParseException("pop3 needs a user name", startIndex);
            int idx = userInfo.indexOf(':');
            if (idx < 0)
                throw new ParseException("pop3 needs a password", startIndex);

            return new POP3Listener(
                    uri.getScheme(),
                    uri.getHost(),
                    uri.getPort()==-1 ? 110 : uri.getPort(),
                    userInfo.substring(0, idx),
                    userInfo.substring(idx + 1),
                    query.getValue("interval", 3000)
            );
        } catch (URISyntaxException e) {
            throw new URISyntaxException(e.getInput(), e.getReason(), e.getIndex() + startIndex);
        }
    }

    private void dump(MimeMessage msg, String caption) {
        System.out.println("---[" + caption + "]---");
        try {
            msg.writeTo(System.out);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception during message dump" , e);
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "Exception during message dump" , e);
        }
        System.out.println("--------------------");
    }

}
