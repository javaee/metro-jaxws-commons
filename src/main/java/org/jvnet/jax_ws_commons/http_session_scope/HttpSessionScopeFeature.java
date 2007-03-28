package org.jvnet.jax_ws_commons.http_session_scope;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.xml.ws.WebServiceFeature;

/**
 * {@link WebServiceFeature} for {@link @HttpSessionScope}.
 * @author Kohsuke Kawaguchi
 */
public class HttpSessionScopeFeature extends WebServiceFeature {
    /**
     * Constant value identifying the {@link @HttpSessionScope} feature.
     */
    public static final String ID = "http://jax-ws-commons.dev.java.net/features/servlet/httpSessionScope";

    @FeatureConstructor
    public HttpSessionScopeFeature() {
        this.enabled = true;
    }

    public String getID() {
        return ID;
    }
}
