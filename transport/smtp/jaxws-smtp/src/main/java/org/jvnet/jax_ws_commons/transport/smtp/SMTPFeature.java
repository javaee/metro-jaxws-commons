package org.jvnet.jax_ws_commons.transport.smtp;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.xml.ws.WebServiceFeature;
import javax.mail.Authenticator;

/**
 * This feature is used to configure outgoing (SMTP) and incomgin servers.
 *
 * @author Jitendra Kotamraju
 * @author Vivek Pandey
 */
public class SMTPFeature extends WebServiceFeature {
    public static final String ID = "http://jax-ws.dev.java.net/smtp/";

    public enum Security {
        NONE, TLS_IFAVAILABLE, TLS, SSL
    }


    public enum IncomingServerType {
        POP3, IMAP
    }


    private String smtpServer;
    private int smtpPort;
    private Security security;
    private Authenticator authenticator;
    private IncomingServer incomingServer;


    @FeatureConstructor
    public SMTPFeature(String server, int port) {
        this.smtpServer = server;
        this.smtpPort = port;
    }

    public String getID() {
        return ID;
    }


    public String getSmtpServer() {
        return smtpServer;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }


    public IncomingServer getIncomingServer() {
        return incomingServer;
    }

    public void setIncomingServer(IncomingServer incomingServer) {
        this.incomingServer = incomingServer;
    }

    public final class POP3 extends IncomingServer {
        public POP3(String server, int port, String emailAddress) {
            super(server, port, emailAddress);
        }

        public IncomingServerType getServerType() {
            return IncomingServerType.POP3;
        }
    }

    public final class Imap extends IncomingServer {
        public Imap(String server, int port, String emailAddress) {
            super(server, port, emailAddress);
        }

        public IncomingServerType getServerType() {
            return IncomingServerType.IMAP;
        }
    }

    /**
     * Configuration bean for an Incoming server. Typically, you would configure the {@link IncomingServer} with
     * the server name , port number and email address. If its a secured server you should also provide additional
     * information such as {@lik Security} and an {@link Authenticator}
     * <p/>
     * TODO: do we also need to provide access to pass {@link javax.net.ssl.SSLSocketFactory}
     */
    public abstract class IncomingServer {
        private final String server;
        private final int port;
        private final String emailAddress;
        private int interval = 3000;
        private Security security = Security.NONE;
        private Authenticator authenticator;


        /**
         * Gives the type {@link IncomingServerType} of incoming server
         */
        public abstract IncomingServerType getServerType();

        public IncomingServer(String server, int port, String emailAddress) {
            this.server = server;
            this.emailAddress = emailAddress;
            this.port = port;
        }

        public void setSecurity(Security security) {
            this.security = security;
        }

        public Security getSecurity() {
            return security;
        }


        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public Authenticator getAuthenticator() {
            return authenticator;
        }

        public void setAuthenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
        }


        public String getServer() {
            return server;
        }

        public int getPort() {
            return port;
        }

        public String getEmailAddress() {
            return emailAddress;
        }
    }
}
