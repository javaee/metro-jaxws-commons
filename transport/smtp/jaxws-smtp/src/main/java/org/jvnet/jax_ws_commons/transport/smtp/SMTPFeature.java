package org.jvnet.jax_ws_commons.transport.smtp;

import javax.xml.ws.WebServiceFeature;

/**
 * This feature is used to configure SMTP transport.
 * 
 * @author Jitendra Kotamraju
 */
public class SMTPFeature extends WebServiceFeature {
    public static final String ID = "http://jax-ws.dev.java.net/smtp/";

    public String getID() {
        return ID;
    }
}
