package com.sun.xml.ws.commons.ec2;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.commons.EC2;
import com.sun.xml.wss.impl.callback.CertStoreCallback;
import com.sun.xml.wss.impl.callback.KeyStoreCallback;
import com.sun.xml.wss.impl.callback.PrivateKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback.PrivKeyCertRequest;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback.Request;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * Code to convince Metro to use our own {@link X509Certificate} and {@link PrivateKey}
 * instead of talking to VM-wide key store.
 *
 * @author Kohsuke Kawaguchi
 */
public class CertStoreCallBackImpl implements CallbackHandler {
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        // I'm just reverse engineering how this method is supposed to work here
        if(callbacks[0] instanceof CertStoreCallback) {
            // noop
            return;
        }
        if (callbacks[0] instanceof KeyStoreCallback) {
            handle((KeyStoreCallback) callbacks[0]);
            return;
        }
        if (callbacks[0] instanceof SignatureKeyCallback) {
            handle((SignatureKeyCallback) callbacks[0]);
            return;
        }
        if (callbacks[0] instanceof PrivateKeyCallback) {
            handle((PrivateKeyCallback) callbacks[0]);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public void handle(SignatureKeyCallback sk) throws IOException {
        Request r = sk.getRequest();
        if (r instanceof PrivKeyCertRequest) {
             PrivKeyCertRequest pkcr = (PrivKeyCertRequest) r;

             Packet p = Fiber.current().getPacket();
             pkcr.setPrivateKey(getPrivateKey(p));
             pkcr.setX509Certificate(getCertificate(p));
             return;
         }
        throw new UnsupportedOperationException();
    }

    private void handle(PrivateKeyCallback pkc) throws IOException {
        Packet p = Fiber.current().getPacket();

        pkc.setKey(getPrivateKey(p));
    }

    private void handle(KeyStoreCallback ksc) throws IOException {
             try {
                 KeyStore ks = KeyStore.getInstance("jks"); // what's 'jks' anyway!?
                 ks.load(null, null); // initialize an empty keystore. brain dead --- why not init() method?

                 Packet p = Fiber.current().getPacket();

                 // alias doesn't matter because we only put one key and Metro is smart enough to find that one
                 ks.setKeyEntry("default", getPrivateKey(p), new char[0], new Certificate[]{
                         getCertificate(p)
                 });

                 ksc.setKeystore(ks);
             } catch (GeneralSecurityException e) {
                 throw new RuntimeException(e); // huh?
             }
         }

    private X509Certificate getCertificate(Packet p) {
        return (X509Certificate) p.invocationProperties.get(CERTIFICATE_PROPERTY);
    }

    private PrivateKey getPrivateKey(Packet p) {
        return (PrivateKey) p.invocationProperties.get(PRIVATEKEY_PROPERTY);
    }

    public static final String PRIVATEKEY_PROPERTY = EC2.class.getName() + ".privateKey";
    public static final String CERTIFICATE_PROPERTY = EC2.class.getName() + ".x509";
}
