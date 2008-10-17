package org.jvnet.jax_ws_commons.transport.grizzly_lwhs.server;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * @author Jitendra Kotamraju
 */
public class LWHSAdapter extends GrizzlyAdapter {
    private final LWHSContext lwhsContext;

    LWHSAdapter(LWHSContext lwhsContext) {
        this.lwhsContext = lwhsContext;
    }

    public void service(GrizzlyRequest request, GrizzlyResponse response) {
        HttpHandler handler = lwhsContext.getHandler();
        LWHSExchange exchange = new LWHSExchange(request, response);
        try {
            handler.handle(exchange);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
