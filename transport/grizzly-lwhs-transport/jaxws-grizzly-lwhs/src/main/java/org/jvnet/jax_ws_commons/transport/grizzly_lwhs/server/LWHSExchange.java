package org.jvnet.jax_ws_commons.transport.grizzly_lwhs.server;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpPrincipal;

import java.net.URI;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * @author Jitendra Kotamraju
 */
public class LWHSExchange extends HttpExchange {
    private GrizzlyRequest request;
    private GrizzlyResponse response;
    private Headers requestHeaders;
    private Headers responseHeaders;

    public LWHSExchange(GrizzlyRequest request, GrizzlyResponse response) {
        this.request = request;
        this.response = response;
        this.requestHeaders = new LWHSRequestHeaders(request);
        this.responseHeaders = new LWHSResponseHeaders(response);
    }

    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    public URI getRequestURI() {
        try {
            return new URI(request.getRequestURI()+"?"+request.getQueryString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRequestMethod() {
        return request.getMethod();
    }

    public HttpContext getHttpContext() {
        return null;
    }

    public void close() {

    }

    public InputStream getRequestBody() {
        try {
            return request.getInputStream();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public OutputStream getResponseBody() {
        try {
            return response.getOutputStream();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {

    }

    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    public int getResponseCode() {
        return 0;
    }

    public InetSocketAddress getLocalAddress() {
        return new InetSocketAddress(request.getLocalAddr(),request.getLocalPort());
    }

    public String getProtocol() {
        return request.getProtocol();
    }

    public Object getAttribute(String name) {
        return null;
    }

    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void setStreams(InputStream i, OutputStream o) {
        throw new UnsupportedOperationException();
    }

    public HttpPrincipal getPrincipal() {
        return null;
    }
}
