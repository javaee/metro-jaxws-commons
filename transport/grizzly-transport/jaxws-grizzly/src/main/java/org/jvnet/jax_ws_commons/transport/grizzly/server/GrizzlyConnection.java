package org.jvnet.jax_ws_commons.transport.grizzly.server;

import com.sun.xml.ws.api.server.WebServiceContextDelegate;

import com.sun.enterprise.web.connector.grizzly.AsyncTask;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.xml.ws.handler.MessageContext;

import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.tomcat.util.http.MimeHeaders;


/**
 * JAX-WS WSHTTPConnection implementation for grizzly transport
 *
 * @author Jitendra Kotamraju
 */

public final class GrizzlyConnection extends WSHTTPConnection implements WebServiceContextDelegate {


    private final Request req;
    private final Response res;

    private int status;

    private boolean outputWritten;
    private boolean isSecure;

    private AsyncTask grizzlyAsyncTask;

    public GrizzlyConnection(@NotNull Request request, @NotNull Response response, AsyncTask grizzlyAsyncTask, boolean isSecure) {
        this.req = request;
        this.res = response;
        this.grizzlyAsyncTask = grizzlyAsyncTask;
        this.isSecure = isSecure;
    }

    @Override
    @Property({MessageContext.HTTP_REQUEST_HEADERS, Packet.INBOUND_TRANSPORT_HEADERS})
    public @NotNull Map<String,List<String>> getRequestHeaders() {
        MimeHeaders mimeHeaders = req.getMimeHeaders();
        return convertHeaders(mimeHeaders);
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
        MimeHeaders mimeHeaders = res.getMimeHeaders();
        return convertHeaders(mimeHeaders);
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
        throw new UnsupportedOperationException("TODO");

    }

    public @NotNull OutputStream getOutput() throws IOException {
        assert !outputWritten;
        outputWritten = true;

        res.setStatus(getStatus());
        throw new UnsupportedOperationException("TODO");

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
        finishGrizzlyResponse();
        super.close();
    }

    protected PropertyMap getPropertyMap() {
        return model;
    }

    void finishGrizzlyResponse() {
        if (grizzlyAsyncTask != null) {
            //JBIGrizzlyAsyncFilter.finishResponse(grizzlyAsyncTask);
            // TODO: setting this to null is a work-around for JAX-WS calling onCompletion / close twice
            // to make sure finish response is only called once
            grizzlyAsyncTask = null;
        }
    }

    /**
     * Convert from MimeHeaders to the format JAX-WS uses
     * with the header name as the map key pointing to a list of values
     * for that header
     *
     * This conversion might be expesive, if this is frequently used it may
     * be worth changing Grizzly or JAX-WS to remove the need for the conversion
     */
    Map<String, List<String>> convertHeaders(MimeHeaders mimeHeaders) {
        Map<String, List<String>> jaxWSHeaders = new HashMap<String, List<String>>();
        Enumeration names = mimeHeaders.names();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            List jaxWSHeaderValues = new ArrayList();
            Enumeration values = mimeHeaders.values(name);
            while (values.hasMoreElements()) {
                String aValue = (String) values.nextElement();
                jaxWSHeaderValues.add(aValue);
            }
            jaxWSHeaders.put(name, jaxWSHeaderValues);
        }

        return jaxWSHeaders;
    }


    private static final PropertyMap model;

    static {
        model = parse(GrizzlyConnection.class);
    }
}
