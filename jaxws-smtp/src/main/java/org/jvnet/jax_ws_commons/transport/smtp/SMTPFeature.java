package org.jvnet.jax_ws_commons.transport.smtp;

import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This feature is used to configure outgoing (SMTP) and incoming servers.
 *
 * <p> Pass this feature to {@link Service} methods while creating the proxy.
 * <p>
 * SMTPFeature feature = new SMTPFeature(...);
 * Hello proxy = HelloService().getHelloPort(feature);
 *
 * @author Jitendra Kotamraju
 * @author Vivek Pandey
 */
public class SMTPFeature extends WebServiceFeature {
    public static final String ID = "http://jax-ws.dev.java.net/smtp/";

    POP3Info pop3;
    SenderInfo senderInfo;

    public SMTPFeature() {
    }

    public SMTPFeature(String smtpHost, String fromAddress) {
        this(smtpHost, null, fromAddress);
    }

    public SMTPFeature(String smtpHost, String smtpPort, String fromAddress) {
        senderInfo = new SenderInfo(smtpHost, smtpPort, fromAddress);
    }

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

    public void setPOP3(String host, String uid, String password) {
        this.pop3 = new POP3Info(host, uid, password);
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
