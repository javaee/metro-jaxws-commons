package org.jvnet.jax_ws_commons.thread_scope;

import javax.xml.ws.WebServiceFeature;

/**
 * {@link WebServiceFeature} for {@link @ThreadScope}.
 * @author Jitendra Kotamraju
 */
public class ThreadScopeFeature extends WebServiceFeature {
    /**
     * Constant value identifying the {@link @ThreadScope} feature.
     */
    public static final String ID = "http://jax-ws-commons.dev.java.net/features/endpoint/thread-scope-instances";

    public ThreadScopeFeature() {
        this.enabled = true;
    }

    public String getID() {
        return ID;
    }
}
