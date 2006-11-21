package com.sun.xml.ws.transport.http.servlet;

import org.springframework.beans.factory.BeanNameAware;
import com.sun.xml.ws.api.server.WSEndpoint;

/**
 * Wraps {@link ServletAdapter} for binding.
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

    public void setUrl(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public void setService(Object endpoint) {
        // this.endpoint = (WSEndpoint<?>) endpoint;
    }

}
