package com.sun.xml.ws.commons.ec2;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.wss.impl.callback.CertStoreCallback;
import com.sun.xml.wss.impl.callback.KeyStoreCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback.PrivKeyCertRequest;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback.Request;
import com.sun.xml.wss.impl.misc.Base64;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @author Kohsuke Kawaguchi
 */
public class CertStoreCallBackImpl implements CallbackHandler {
    private File privateKey,x509;

    public CertStoreCallBackImpl(File privateKey, File x509) {
        this.privateKey = privateKey;
        this.x509 = x509;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        // I'm just reverse engineering how this method is supposed to work here
        if(callbacks[0] instanceof CertStoreCallback) {
            handle((CertStoreCallback)callbacks[0]);
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
        throw new UnsupportedOperationException();
    }

    public void handle(SignatureKeyCallback sk) throws IOException {
        try {
            Request r = sk.getRequest();
            if (r instanceof PrivKeyCertRequest) {
                PrivKeyCertRequest pkcr = (PrivKeyCertRequest) r;
                pkcr.setPrivateKey(loadKey(privateKey));
                X509Certificate cert = loadX509Certificate(x509);
                pkcr.setX509Certificate(cert);
                return;
            }
            throw new UnsupportedOperationException();
        } catch (GeneralSecurityException e) {
            IOException x = new IOException("Invalid key file");
            x.initCause(e);
            throw x;
        }
    }

    public void handle(CertStoreCallback csc) {
//        CertStore store = CertStore.getInstance(
//        csc.setCertStore(store);
    }

    public void handle(KeyStoreCallback ksc) throws IOException {
        try {
            KeyStore ks = KeyStore.getInstance("jks"); // what's 'jks' anyway!?
            ks.load(null,null); // initialize an empty keystore. brain dead --- why not init() method?

            // alias doesn't matter because we only put one key and Metro is smart enough to find that one
            ks.setKeyEntry("default", loadKey(privateKey),null,new Certificate[]{loadX509Certificate(x509)});
            
            ksc.setKeystore(ks);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e); // huh?
        }
    }

    private X509Certificate loadX509Certificate(File certificate) throws GeneralSecurityException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X509");
        return (X509Certificate) factory.generateCertificate(new FileInputStream(certificate));
    }

    private PrivateKey loadKey(File keyfile) throws IOException, GeneralSecurityException {
        StringBuilder keyBuf = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(keyfile));
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith("-----") && !line.endsWith("-----"))
                keyBuf.append(line);
        }
        br.close();

        try {
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(Base64.decode(keyBuf.toString()));

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(privKeySpec);
        } catch (Base64DecodingException e) {
            IOException x = new IOException("Invalid key file");
            x.initCause(e);
            throw x;
        }
    }
}
