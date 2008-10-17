package org.jvnet.jax_ws_commons.transport.grizzly_lwhs.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * @author Jitendra Kotamraju
 */
public class LWHSServer extends HttpServer {

    public LWHSServer(InetSocketAddress addr, int backlog) {
    }

    public void bind(InetSocketAddress addr, int backlog) throws IOException {
    }

    public void start() {

    }

    public void setExecutor(Executor executor) {

    }

    public Executor getExecutor() {
        return null;
    }

    public void stop(int delay) {

    }

    public HttpContext createContext(String path, HttpHandler handler) {
        return new LWHSContext(this, path, handler);
    }

    public HttpContext createContext(String path) {
        return new LWHSContext(this, path, null);
    }

    public void removeContext(String path) throws IllegalArgumentException {

    }

    public void removeContext(HttpContext context) {

    }

    public InetSocketAddress getAddress() {
        return null;
    }
}
