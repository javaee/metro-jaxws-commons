package org.jvnet.jax_ws_commons.transport.smtp.server;

import org.jvnet.jax_ws_commons.transport.smtp.SMTPFeature;
import org.jvnet.jax_ws_commons.transport.smtp.mail.EmailEndpoint;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import com.sun.xml.ws.api.server.WSEndpoint;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Spring configures this bean with all the SMTP related configuration for sending and
 * receiving emails.
 *
 * @org.apache.xbean.XBean element="smtp" root-element="true"
 * @author Jitendra Kotamraju
 */
public class ServerSMTPFeature extends SMTPFeature implements FactoryBean, InitializingBean {
    private static final Logger logger = Logger.getLogger(ServerSMTPFeature.class.getName());

    private SMTPAdapter adapter;
    private WSEndpoint<?> endpoint;

    /**
     * The service to be bound to the specified URL.
     *
     * @org.xbean.Property required="true"
     */
    public void setService(WSEndpoint<?> endpoint) {
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
        //String add = "smtp://smtp.transport.client@kohsuke.org?!pop3://smtp.transport.server:jaxws123@kohsuke.org/";
        EmailEndpoint email = new EmailEndpoint(this);
        adapter = new SMTPAdapter(endpoint);
        adapter.setEmailEndpoint(email);
        adapter.start();
        logger.info("*** SMTPAdapter Started for "+endpoint.getImplementationClass()+" ***");
    }
}
