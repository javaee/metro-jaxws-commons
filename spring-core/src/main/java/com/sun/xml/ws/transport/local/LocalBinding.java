package com.sun.xml.ws.transport.local;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.servlet.SpringBinding;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;

import java.io.IOException;
import java.util.List;

/**
 * Set of {@link SpringBinding}.
 *
 * @author Kohsuke Kawaguchi
 * @org.apache.xbean.XBean element="bindings" rootElement="true"
 */
public class LocalBinding implements BeanNameAware, FactoryBean {
    private List<WSEndpoint> endpoints;
    private String name;
    private InVmServer server;

    public List<WSEndpoint> getEndpoints() {
        return endpoints;
    }

    /**
     * Individual endpoints.
     *
     * @org.apache.xbean.Property nestedType="com.sun.xml.ws.api.server.WSEndpoint"
     */
    public void setEndpoints(List<WSEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * Bean name is used as the ID of the in-vm server,
     * which becomes the URI to access this in-vm server endpoint
     * (<tt>in-vm://<i>ID</i>/</tt>)
     */
    public void setBeanName(String name) {
        this.name = name;
    }

    /**
     * Obtains the fully-configured {@link InVmServer}.
     */
    public InVmServer getObject() throws IOException {
        if(server==null)
            server = new InVmServer(name,endpoints);
        return server;
    }

    public Class getObjectType() {
        return InVmServer.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
