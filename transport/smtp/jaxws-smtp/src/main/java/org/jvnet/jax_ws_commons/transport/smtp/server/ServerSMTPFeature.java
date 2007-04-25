package org.jvnet.jax_ws_commons.transport.smtp.server;

import org.jvnet.jax_ws_commons.transport.smtp.SMTPFeature;
import org.jvnet.jax_ws_commons.transport.smtp.mail.EmailEndpoint;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import com.sun.xml.ws.api.server.WSEndpoint;

import java.util.Properties;

/**
 * @author Jitendra Kotamraju
 */

/**
 * @org.apache.xbean.XBean element="smtp" root-element="true"
 */
public class ServerSMTPFeature extends SMTPFeature implements FactoryBean, InitializingBean {
    private SMTPAdapter adapter;
    private WSEndpoint<?> endpoint;

    /**
     * The service to be bound to the specified URL.
     *
     * @org.xbean.Property required="true"
     */
    public void setService(WSEndpoint<?> endpoint) {
        System.out.println("**** endpoint ***="+endpoint);
        this.endpoint = endpoint;
    }

    public Class getObjectType() {
        return SMTPAdapter.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public SMTPAdapter getObject() throws Exception {
        return adapter;
    }

    public void afterPropertiesSet() throws Exception {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", "kohsuke.sfbay.sun.com");   // TODO
        props.put("mail.smtp.port", "10025");

        String add = "smtp://smtp.transport.client@kohsuke.org?!pop3://smtp.transport.server:jaxws123@kohsuke.org/";
        adapter = new SMTPAdapter(endpoint);
        adapter.setEmailEndpoint(new EmailEndpoint(add));
        adapter.start();
        System.out.println("**** SMTPAdapter started ****");
    }
}
