package org.jvnet.jax_ws_commons.transport.grizzly_lwhs.server;

import com.sun.net.httpserver.*;

import java.util.Map;
import java.util.List;

/**
 * @author Jitendra Kotamraju
 */
public class LWHSContext extends HttpContext {
    private final HttpServer server;
    private final String path;
    private HttpHandler handler;
    private Authenticator auth;

    public LWHSContext(HttpServer server, String path, HttpHandler handler) {
        this.server = server;
        this.path = path;
        this.handler = handler;
    }

    public HttpHandler getHandler() {
        return handler;
    }

    public void setHandler(HttpHandler h) {
        this.handler = h;
    }

    public String getPath() {
        return path;
    }

    public HttpServer getServer() {
        return server;
    }

    public Map<String, Object> getAttributes() {
        return null;
    }

    public List<Filter> getFilters() {
        return null;
    }

    public Authenticator setAuthenticator(Authenticator auth) {
        this.auth = auth;
        return auth;
    }

    public Authenticator getAuthenticator() {
        return auth;
    }
}
