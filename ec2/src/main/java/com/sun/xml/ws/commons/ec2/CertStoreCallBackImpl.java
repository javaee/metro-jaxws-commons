package com.sun.xml.ws.commons.ec2;

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

    private final PrivateKey privateKey;
    private final X509Certificate certificate;

    public CertStoreCallBackImpl(PrivateKey privateKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

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

             pkcr.setPrivateKey(this.privateKey);
             pkcr.setX509Certificate(this.certificate);
             return;
         }
        throw new UnsupportedOperationException();
    }

    private void handle(PrivateKeyCallback pkc) throws IOException {
        pkc.setKey(this.privateKey);
    }

    private void handle(KeyStoreCallback ksc) throws IOException {
        try {
             KeyStore ks = KeyStore.getInstance("jks"); // what's 'jks' anyway!?
             ks.load(null, null); // initialize an empty keystore. brain dead --- why not init() method?

             // alias doesn't matter because we only put one key and Metro is smart enough to find that one
             ks.setKeyEntry("default", this.privateKey, new char[0], new Certificate[]{this.certificate});

             ksc.setKeystore(ks);
         } catch (GeneralSecurityException e) {
             throw new RuntimeException(e); // huh?
         }
    }

}