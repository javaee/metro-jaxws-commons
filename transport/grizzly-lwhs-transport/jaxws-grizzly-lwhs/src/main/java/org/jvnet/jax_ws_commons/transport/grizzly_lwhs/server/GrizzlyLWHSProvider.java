package org.jvnet.jax_ws_commons.transport.grizzly_lwhs.server;

import com.sun.net.httpserver.spi.HttpServerProvider;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import java.net.InetSocketAddress;
import java.io.IOException;

/**
 * @author Jitendra Kotamraju
 */
public class GrizzlyLWHSProvider extends HttpServerProvider {
    public HttpServer createHttpServer(InetSocketAddress addr, int backlog) throws IOException {
        return new LWHSServer(addr, backlog);
    }

    public HttpsServer createHttpsServer(InetSocketAddress addr, int backlog) throws IOException {
        throw new UnsupportedOperationException();
    }
}
