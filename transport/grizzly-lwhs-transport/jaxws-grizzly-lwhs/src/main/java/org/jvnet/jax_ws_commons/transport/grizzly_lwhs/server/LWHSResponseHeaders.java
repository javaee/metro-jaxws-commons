package org.jvnet.jax_ws_commons.transport.grizzly_lwhs.server;

import com.sun.net.httpserver.Headers;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;

/**
 * @author Jitendra Kotamraju
 */
public class LWHSResponseHeaders extends Headers {
    private GrizzlyResponse response;

    public LWHSResponseHeaders(GrizzlyResponse response) {
        this.response = response;
    }
}
