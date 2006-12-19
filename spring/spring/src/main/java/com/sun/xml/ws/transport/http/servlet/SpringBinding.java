package com.sun.xml.ws.transport.http.servlet;

import org.springframework.beans.factory.BeanNameAware;
import com.sun.xml.ws.api.server.WSEndpoint;

/**
 * Represents the association between the service and URL.
 *
 * @author Kohsuke Kawaguchi
 * @org.apache.xbean.XBean element="binding"
 */
public class SpringBinding implements BeanNameAware {
    private String beanName;
    private String urlPattern;
    private WSEndpoint<?> endpoint;

    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void create(ServletAdapterList owner) {
        String name = beanName;
        if(name==null)      name=urlPattern;
        owner.createHttpAdapter(name, urlPattern, endpoint);
    }

    /**
     * URL pattern to which this service is bound.
     *
     * @org.xbean.Property required="true"
     */
    public void setUrl(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    /**
     * The service to be bound to the specified URL.
     *
     * @org.xbean.Property required="true"
     */
    public void setService(WSEndpoint<?> endpoint) {
        this.endpoint = endpoint;
    }

}
