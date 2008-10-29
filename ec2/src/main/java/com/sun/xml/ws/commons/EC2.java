package com.sun.xml.ws.commons;

import com.sun.xml.ws.commons.ec2.AmazonEC2;
import com.sun.xml.ws.commons.ec2.AmazonEC2PortType;
import com.sun.xml.ws.commons.ec2.CertStoreCallBackImpl;
import com.sun.xml.ws.api.security.CallbackHandlerFeature;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.io.File;
import java.net.URL;

/**
 * Entry point to the Amazon EC2 functionality.
 * @author Kohsuke Kawaguchi
 */
public class EC2 {
    public static AmazonEC2PortType connect(File privateKey, File x509certificate) {
        URL wsdl = EC2.class.getClassLoader().getResource("ec2.wsdl");
        if(wsdl==null)
            throw new LinkageError("ec2.wsdl not found, but it should have been in the jar");
        AmazonEC2 svc = new AmazonEC2(wsdl,new QName("http://ec2.amazonaws.com/doc/2008-08-08/", "AmazonEC2"));
        AmazonEC2PortType port = svc.getAmazonEC2Port(new CallbackHandlerFeature(new CertStoreCallBackImpl(privateKey, x509certificate)));
//        ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,"http://localhost:12345/");
        return port;
    }

}
