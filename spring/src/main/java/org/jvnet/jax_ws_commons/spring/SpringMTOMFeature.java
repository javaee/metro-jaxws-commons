package org.jvnet.jax_ws_commons.spring;

import org.springframework.beans.factory.FactoryBean;

import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.MTOM;

/**
 * Configures MTOM feature.
 *
 * @org.apache.xbean.XBean element="mtom"
 * @author Kohsuke Kawaguchi
 */
// this kind of wrapping is unnecessary if we can perform XBean processing on the feature bean
// itself. But MTOMFeature is a spec feature and we can't do anything about it, so this is a wrapper
public class SpringMTOMFeature implements FactoryBean {

    private boolean enabled = true;
    private int threshold;

    private MTOMFeature feature;

    public boolean isSingleton() {
        return true;
    }

    /**
     * <tt>enabled="false"</tt> can be specified to override the {@link MTOM} annotation
     * on the source code. Defaults to true.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The size in bytes that binary data SHOULD be before
     * being sent as an attachment.
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public MTOMFeature getObject() throws Exception {
        if(feature==null)
            feature = new MTOMFeature(enabled,threshold);
        return feature;
    }

    public Class getObjectType() {
        return MTOMFeature.class;
    }
}
