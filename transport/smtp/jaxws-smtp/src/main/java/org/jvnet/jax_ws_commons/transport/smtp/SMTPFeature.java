package org.jvnet.jax_ws_commons.transport.smtp;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.xml.ws.WebServiceFeature;

/**
 * This feature is used to configure SMTP transport.
 * 
 * @author Jitendra Kotamraju
 */
public class SMTPFeature extends WebServiceFeature {
    public static final String ID = "http://jax-ws.dev.java.net/smtp/";

    @FeatureConstructor
    public SMTPFeature() {
    }

    public String getID() {
        return ID;
    }
}
