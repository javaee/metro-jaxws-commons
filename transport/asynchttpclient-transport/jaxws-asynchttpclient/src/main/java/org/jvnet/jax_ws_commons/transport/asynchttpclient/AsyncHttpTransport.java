/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.jvnet.jax_ws_commons.transport.asynchttpclient;

import com.ning.http.client.*;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.resources.ClientMessages;
import com.sun.xml.ws.transport.Headers;
import com.sun.xml.ws.transport.http.client.CookieJar;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.RuntimeVersion;
import org.jvnet.jax_ws_commons.transport.asynchttpclient.AsyncHttpTransportTube.AsyncCallbackImpl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;
/**
 *
 * @author WS Development Team
 */
final class AsyncHttpTransport {
    // Need to use JAXB first to register DatatypeConverter
    static {
        try {
            JAXBContext.newInstance().createUnmarshaller();
        } catch(JAXBException je) {
            // Nothing much can be done. Intentionally left empty
        }
    }

    /*package*/ int statusCode;
    /*package*/ String statusMessage;
    private final Map<String, List<String>> reqHeaders;
    private Map<String, List<String>> respHeaders;
    private Response response;
    private RequestBuilder requestBuilder;
    private AsyncHttpClient asyncClient;
    private OutputStream outputStream;
    private boolean https;
    private final EndpointAddress endpoint;
    private final Packet context;
    private String method;

    AsyncCallbackImpl callback;

    public AsyncHttpTransport(@NotNull Packet packet, @NotNull Map<String,List<String>> reqHeaders) {
        endpoint = packet.endpointAddress;
        context = packet;
        this.reqHeaders = reqHeaders;
    }

    /*
     * Prepare the stream for HTTP request
     */
    public void writeOutput(final Codec codec, final Packet p) {
        try {
            createHttpConnection();
            if (requiresOutputStream()) {
                requestBuilder.setBody(new Request.EntityWriter(){
                    public void writeEntity(OutputStream out) throws IOException {
                        codec.encode(p, out);
                    }
                });
            }

            asyncClient.executeRequest(requestBuilder.build(), new AsyncCompletionHandlerBase() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    AsyncHttpTransport.this.response = response;
                    callback.send();
                    return response;
                }
            });
        } catch (Exception ex) {
            throw new ClientTransportException(
                ClientMessages.localizableHTTP_CLIENT_FAILED(ex),ex);
        }

    }

    void setCallback(AsyncCallbackImpl callback) {
        this.callback = callback;
    }

    /*
     * Prepare the stream for HTTP request
     */
    public OutputStream writeOutput(ByteArrayBuffer b) {
        try {
            createHttpConnection();
            if (requiresOutputStream()) {
                requestBuilder.setBody(b.newInputStream());
            }
            asyncClient.executeRequest(requestBuilder.build(), new AsyncCompletionHandlerBase() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    AsyncHttpTransport.this.response = response;
                    callback.send();
                    return response;
                }
            });
        } catch (Exception ex) {
            throw new ClientTransportException(
                ClientMessages.localizableHTTP_CLIENT_FAILED(ex),ex);
        }
        return outputStream;
    }

    public void closeOutput() throws IOException {
        if (outputStream != null) {
            outputStream.close();
            outputStream = null;
        }
    }

    /*
     * Get the response from HTTP connection and prepare the input stream for response
     */
    public @Nullable InputStream getInput() {
        return readResponse();
    }

    public Map<String, List<String>> getHeaders() {
        if (respHeaders != null) {
            return respHeaders;
        }
        respHeaders = new Headers();
        for(Map.Entry<String, List<String>> entry : response.getHeaders()) {
            respHeaders.put(entry.getKey(), entry.getValue());
        }
        return respHeaders;
    }

    protected @Nullable InputStream readResponse() {
        try {
            return response.getResponseBodyAsStream();
        } catch(IOException ioe) {
            throw new WebServiceException(ioe);
        }
    }

    protected void readResponseCodeAndMessage() {
        statusCode = response.getStatusCode();
        statusMessage = response.getStatusText();
    }

    private void createHttpConnection() throws IOException {
        AsyncHttpClientConfig.Builder asyncConfigBuilder = new AsyncHttpClientConfig.Builder();
        Integer reqTimeout = (Integer)context.invocationProperties.get(BindingProviderProperties.REQUEST_TIMEOUT);
        if (reqTimeout != null) {
            asyncConfigBuilder.setRequestTimeoutInMs(reqTimeout);
        }

        Integer connectTimeout = (Integer)context.invocationProperties.get(JAXWSProperties.CONNECT_TIMEOUT);
        if (connectTimeout != null) {
            asyncConfigBuilder.setConnectionTimeoutInMs(connectTimeout);
        }
        asyncConfigBuilder.setUserAgent(RuntimeVersion.VERSION.toString());

        List<String> contentEncoding = reqHeaders.get("Content-Encoding");

        if (contentEncoding != null && contentEncoding.get(0).contains("gzip")) {
            asyncConfigBuilder.setCompressionEnabled(true);
        }

        asyncClient = new AsyncHttpClient(asyncConfigBuilder.build());

        String requestMethod = (String) context.invocationProperties.get(MessageContext.HTTP_REQUEST_METHOD);
        method = (requestMethod != null) ? requestMethod : "POST";

        requestBuilder = new RequestBuilder(RequestType.valueOf(method));
        requestBuilder.setUrl(endpoint.toString());
        writeBasicAuthAsNeeded(context, reqHeaders);
        for (Map.Entry<String, List<String>> entry : reqHeaders.entrySet()) {
            for (String value : entry.getValue()) {
                requestBuilder.addHeader(entry.getKey(), value);
            }
        }

    }

    public boolean isSecure() {
        return https;
    }

    private void writeBasicAuthAsNeeded(Packet context, Map<String, List<String>> reqHeaders) {
        String user = (String) context.invocationProperties.get(BindingProvider.USERNAME_PROPERTY);
        if (user != null) {
            String pw = (String) context.invocationProperties.get(BindingProvider.PASSWORD_PROPERTY);
            if (pw != null) {
                StringBuffer buf = new StringBuffer(user);
                buf.append(":");
                buf.append(pw);
                String creds = printBase64Binary(buf.toString().getBytes());
                reqHeaders.put("Authorization", Collections.singletonList("Basic "+creds));
            }
        }
    }

    private boolean requiresOutputStream() {
        return !(method.equalsIgnoreCase("GET") ||
                method.equalsIgnoreCase("HEAD") ||
                method.equalsIgnoreCase("DELETE"));
    }

    public @Nullable String getContentType() {
        return response.getContentType();
    }

}

