package org.jvnet.jax_ws_commons.transport.grizzly_lwhs.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.grizzly.http.embed.GrizzlyWebServer;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * @author Jitendra Kotamraju
 */
public class LWHSServer extends HttpServer {
    private GrizzlyWebServer server;
    private InetSocketAddress addr;
    private LWHSContext lwhsContext;
    private Executor executor;

    public LWHSServer(InetSocketAddress addr, int backlog) {
        this.addr = addr;
    }

    public void bind(InetSocketAddress addr, int backlog) throws IOException {
        this.addr = addr;
    }

    public void start() {
        try {
            if (server != null) {
                server.addGrizzlyAdapter(new LWHSAdapter(lwhsContext)); 
                server.start();
            }
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void stop(int delay) {
        if (server != null) {
            server.stop();
        }
    }

    public HttpContext createContext(String path, HttpHandler handler) {
        server = new GrizzlyWebServer(addr.getPort(), path);
        lwhsContext = new LWHSContext(this, path, handler);
        return lwhsContext;
    }

    public HttpContext createContext(String path) {
        server = new GrizzlyWebServer(addr.getPort(), path);
        lwhsContext = new LWHSContext(this, path, null);
        return lwhsContext;
    }

    public void removeContext(String path) throws IllegalArgumentException {

    }

    public void removeContext(HttpContext context) {

    }

    public InetSocketAddress getAddress() {
        return addr;
    }
}
