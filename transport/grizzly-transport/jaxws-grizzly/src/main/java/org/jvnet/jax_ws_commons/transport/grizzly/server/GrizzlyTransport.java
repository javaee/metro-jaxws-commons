package org.jvnet.jax_ws_commons.transport.grizzly.server;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.async.DefaultAsyncHandler;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Spring configures this bean with all the grizzly related configuration for
 * receiving webservice requests.
 *
 * @org.apache.xbean.XBean element="grizzly" root-element="true"
 * @author Jitendra Kotamraju
 */
public class GrizzlyTransport implements FactoryBean, InitializingBean {
    private static final Logger logger = Logger.getLogger(GrizzlyTransport.class.getName());

    private HttpAdapter adapter;
    private WSEndpoint<?> endpoint;
    private int port;

    /**
     * The service to be bound to the specified URL.
     *
     * @org.xbean.Property required="true"
     */
    public void setService(WSEndpoint<?> endpoint) {
        this.endpoint = endpoint;
    }

    public Class getObjectType() {
        return HttpAdapter.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public HttpAdapter getObject() throws Exception {
        return adapter;
    }

    public void afterPropertiesSet() throws Exception {
        adapter = HttpAdapter.createAlone(endpoint);
        SelectorThread st = new SelectorThread();
        st.setPort(port);
        DefaultAsyncHandler ah = new DefaultAsyncHandler();
        st.setAsyncHandler(ah);
        st.setEnableAsyncExecution(true);
        ah.addAsyncFilter(new GrizzlyAsyncFilter());

        st.initEndpoint();
        st.startEndpoint();
        //adapter.start();
        logger.info("*** HttpAdapter Started for "+endpoint.getImplementationClass()+" ***");
    }
}
