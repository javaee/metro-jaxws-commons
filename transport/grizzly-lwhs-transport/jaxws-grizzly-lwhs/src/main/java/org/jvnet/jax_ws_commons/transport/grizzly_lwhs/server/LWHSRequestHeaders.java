package org.jvnet.jax_ws_commons.transport.grizzly_lwhs.server;

import com.sun.net.httpserver.Headers;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;

/**
 * @author Jitendra Kotamraju
 */
public class LWHSRequestHeaders extends Headers {
    private GrizzlyRequest request;

    public LWHSRequestHeaders(GrizzlyRequest request) {
        this.request = request;
    }
}
