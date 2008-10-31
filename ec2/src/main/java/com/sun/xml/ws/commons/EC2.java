package com.sun.xml.ws.commons;

import com.sun.xml.ws.commons.ec2.AmazonEC2;
import com.sun.xml.ws.commons.ec2.AmazonEC2PortType;
import com.sun.xml.ws.commons.ec2.CertStoreCallBackImpl;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.cert.CertStore;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Entry point to the Amazon EC2 functionality.
 * @author Kohsuke Kawaguchi
 */
public class EC2 {
    /**
     * Creates a JAX-WS proxy object that you can use to talk to EC2.
     *
     * @param privateKey
     *      Private key that proves your identity to EC2. This is the "pk-*.pem" file.
     * @param x509certificate
     *      X509 certificate, which is the public key. This is the "cert-*.pem" file.
     * @return
     *      A proxy object that exposes EC2 SOAP API as method calls. This object is multi-thread safe.
     * 
     * @throws IOException
     *      If key files fail to load.
     * @throws GeneralSecurityException
     *      If cryptography related problem is encounted while handling the key.
     * @throws WebServiceException
     *      If Metro fails.
     */
    public static AmazonEC2PortType connect(File privateKey, File x509certificate) throws IOException, GeneralSecurityException {
        URL wsdl = EC2.class.getClassLoader().getResource("ec2.wsdl");
        if(wsdl==null)
            throw new LinkageError("ec2.wsdl not found, but it should have been in the jar");
        AmazonEC2 svc = new AmazonEC2(wsdl,new QName("http://ec2.amazonaws.com/doc/2008-08-08/", "AmazonEC2"));
        // TODO: when Metro hits 1.5 we can use CallbackHandlerFeature
//        return svc.getAmazonEC2Port(new CallbackHandlerFeature(new CertStoreCallBackImpl(privateKey, x509certificate)));

        AmazonEC2PortType port = svc.getAmazonEC2Port();
        ((BindingProvider)port).getRequestContext().put(CertStoreCallBackImpl.PRIVATEKEY_PROPERTY,loadKey(privateKey));
        ((BindingProvider)port).getRequestContext().put(CertStoreCallBackImpl.CERTIFICATE_PROPERTY,loadX509Certificate(x509certificate));
        ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,"https://ec2.amazonaws.com/");
        return port;
    }

    private static X509Certificate loadX509Certificate(File certificate) throws GeneralSecurityException, IOException {
             CertificateFactory factory = CertificateFactory.getInstance("X509");
             return (X509Certificate) factory.generateCertificate(new FileInputStream(certificate));
         }

    private static PrivateKey loadKey(File keyfile) throws IOException, GeneralSecurityException {
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
