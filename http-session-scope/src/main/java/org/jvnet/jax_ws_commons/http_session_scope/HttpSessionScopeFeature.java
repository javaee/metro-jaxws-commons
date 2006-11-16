package org.jvnet.jax_ws_commons.http_session_scope;

import javax.xml.ws.WebServiceFeature;

/**
 * {@link WebServiceFeature} for {@link @HttpSessionScope}.
 * @author Kohsuke Kawaguchi
 */
public class HttpSessionScopeFeature extends WebServiceFeature {
    /**
     * Constant value identifying the {@link @HttpSessionScope} feature.
     */
    public static final String ID = "http://jax-ws.dev.java.net/features/servlet/httpSessionScope";

    public HttpSessionScopeFeature() {
        this.enabled = true;
    }

    public String getID() {
        return ID;
    }
}
