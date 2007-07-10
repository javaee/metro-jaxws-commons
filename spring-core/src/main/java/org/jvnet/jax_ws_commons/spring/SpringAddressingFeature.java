package org.jvnet.jax_ws_commons.spring;

import org.springframework.beans.factory.FactoryBean;

import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.AddressingFeature;

/**
 * Configures WS-Addressing feature.
 *
 * @org.apache.xbean.XBean element="addressing"
 * @author Kohsuke Kawaguchi
 */
// See SpringMTOMFeature why this is needed
public class SpringAddressingFeature implements FactoryBean {

    private boolean enabled = true;
    private boolean required;

    private AddressingFeature feature;

    public boolean isSingleton() {
        return true;
    }

    /**
     * <tt>enabled="false"</tt> can be specified to override the {@link Addressing} annotation
     * on the source code. Defaults to true.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * If true, the service will reject all requests that do not have addressing headers.
     * False to accept them. Defaults to false. 
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    public AddressingFeature getObject() throws Exception {
        if(feature==null)
            feature = new AddressingFeature(enabled,required);
        return feature;
    }

    public Class getObjectType() {
        return AddressingFeature.class;
    }
}
