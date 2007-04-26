package org.jvnet.jax_ws_commons.transport.smtp;

import javax.xml.ws.WebServiceFeature;

/**
 * This feature is used to configure outgoing (SMTP) and incomgin servers.
 *
 * @author Jitendra Kotamraju
 * @author Vivek Pandey
 */
public class SMTPFeature extends WebServiceFeature {
    public static final String ID = "http://jax-ws.dev.java.net/smtp/";

    POP3Info pop3;
    SenderInfo senderInfo;

    public String getID() {
        return ID;
    }
    
    /**
     * @org.xbean.Property required="true"
     */
    public void setIncoming(POP3Info pop3) {
        this.pop3 = pop3;
    }

    public POP3Info getIncoming() {
        return pop3;
    }

    /**
     * @org.xbean.Property required="true"
     */
    public void setOutgoing(SenderInfo senderInfo) {
        this.senderInfo = senderInfo;
    }

    public SenderInfo getOutgoing() {
        return senderInfo;
    }

}
