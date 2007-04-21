package org.jvnet.jax_ws_commons.transport.smtp.server;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.jvnet.jax_ws_commons.transport.smtp.mail.EmailEndpoint;
import com.sun.xml.ws.api.server.WSEndpoint;

import java.util.Properties;

/**
 * @org.apache.xbean.XBean element="smtp"
 */
public class SMTPConfig implements BeanNameAware, InitializingBean {
    private String name;
    private String address;
    
    public void setBeanName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
        System.out.println("**** Address ***="+address);
    }

    public String getAddress() {
        return address;
    }

    public void afterPropertiesSet() throws Exception {
        System.out.println("**** Start the SMTPAdapter ****");
        try {
        // Try the transport with some values until we figure out spring
        Class cls = Class.forName("greeter.GreetingService");
        WSEndpoint endpoint = WSEndpoint.create(cls,false, null, null, null, null, null, null, null, null, false);
        SMTPAdapter adapter = new SMTPAdapter(endpoint);
        Properties props = System.getProperties();
        props.put("mail.smtp.host","smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable","true");
        String add = "smtp://smtp.transport:beat.net@gmail.com?mail.pop3.socketFactory.class=javax.net.ssl.SSLSocketFactory&mail.pop3.port=995!pop3s://smtp.transport:beat.net@pop.gmail.com/";
        adapter.setEmailEndpoint(new EmailEndpoint(add));
        adapter.start();
        System.out.println("**** SMTPAdapter started ****");
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
