package org.jvnet.jax_ws_commons.transport.grizzly.server;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.List;
import java.util.Map;


/**
 * JAX-WS WSHTTPConnection implementation for grizzly transport
 *
 * @author Jitendra Kotamraju
 */

public final class JaxwsGrizzlyConnection extends WSHTTPConnection implements WebServiceContextDelegate {


    private final GrizzlyRequest req;
    private final GrizzlyResponse res;

    private int status;

    private boolean outputWritten;
    private boolean isSecure;

    public JaxwsGrizzlyConnection(@NotNull GrizzlyRequest request, @NotNull GrizzlyResponse response, boolean isSecure) {
        this.req = request;
        this.res = response;
        this.isSecure = isSecure;
    }

    @Override
    @Property({MessageContext.HTTP_REQUEST_HEADERS, Packet.INBOUND_TRANSPORT_HEADERS})
    public @NotNull Map<String,List<String>> getRequestHeaders() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String getRequestHeader(String headerName) {
        return req.getHeader(headerName);
    }

    @Override
    public void setResponseHeaders(Map<String,List<String>> headers) {
        if (headers != null) {
            for (Map.Entry<String,List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                if (values.size() == 1) {
                    res.setHeader(key, values.get(1));
                } else {
                    // If the header has multiple values, comma separte them
                    StringBuffer concat = new StringBuffer();
                    boolean firstTime = true;
                    for (String aValue : values) {
                        if (!firstTime) {
                            concat.append(',');
                        }
                        concat.append(aValue);
                        firstTime = false;
                    }
                    res.setHeader(key, concat.toString());
                }
            }
        }
    }

    @Override
    @Property(MessageContext.HTTP_RESPONSE_HEADERS)
    public Map<String,List<String>> getResponseHeaders() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void setContentTypeResponseHeader(@NotNull String value) {
        res.setHeader("Content-Type", value);
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    @Property(MessageContext.HTTP_RESPONSE_CODE)
    public int getStatus() {
        return status;
    }

    public @NotNull InputStream getInput() throws IOException{
        return req.getInputStream();
    }

    public @NotNull OutputStream getOutput() throws IOException {
        assert !outputWritten;
        outputWritten = true;

        res.setStatus(getStatus());
        return res.getOutputStream();
    }

    public @NotNull WebServiceContextDelegate getWebServiceContextDelegate() {
        return this;
    }

    public Principal getUserPrincipal(Packet request) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isUserInRole(Packet request, String role) {
        return false;
    }

    public @NotNull String getEPRAddress(Packet request, WSEndpoint endpoint) {
        throw new UnsupportedOperationException("TODO");
    }

    public String getWSDLAddress(@NotNull Packet request, @NotNull WSEndpoint endpoint) {
        String eprAddress = getEPRAddress(request,endpoint);
        return eprAddress + "?wsdl";
    }

    //@Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    @Property(MessageContext.HTTP_REQUEST_METHOD)
    public @NotNull String getRequestMethod() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Property(MessageContext.QUERY_STRING)
    public String getQueryString() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Property(MessageContext.PATH_INFO)
    public String getPathInfo() {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Override the close to make sure the Grizzly ARP processing completes
     * Delegate further processing to parent class
     */
    public void close() {
        super.close();
    }

    protected PropertyMap getPropertyMap() {
        return model;
    }

    private static final PropertyMap model;

    static {
        model = parse(JaxwsGrizzlyConnection.class);
    }
}
