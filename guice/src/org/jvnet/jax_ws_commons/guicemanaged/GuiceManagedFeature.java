package org.jvnet.jax_ws_commons.guicemanaged;

import com.sun.xml.ws.api.FeatureConstructor;
import javax.xml.ws.WebServiceFeature;

/**
 * The feature, just holds a unique ID and sets the enabled flag.
 *
 * @author Marcus Eriksson, krummas@gmail.com
 * @since Nov 4, 2008
 */
public class GuiceManagedFeature extends WebServiceFeature {
    public static final String ID="marcus.guice.managed.feature";

    @FeatureConstructor
    public GuiceManagedFeature()
    {
        this.enabled=true;
    }
    
    public String getID() {
        return ID;
    }
}
