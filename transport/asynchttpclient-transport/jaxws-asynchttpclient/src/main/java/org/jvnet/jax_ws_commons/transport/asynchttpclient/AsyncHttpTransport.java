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
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.client.BindingProviderProperties;
import static com.sun.xml.ws.client.BindingProviderProperties.*;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.resources.ClientMessages;
import com.sun.xml.ws.transport.Headers;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.transport.http.client.CookieJar;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.RuntimeVersion;
import com.sun.istack.Nullable;
import com.sun.istack.NotNull;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingProvider;
import static javax.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;
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
    private Map<String, List<String>> respHeaders = null;
    private Response response;
    private RequestBuilder requestBuilder;
    private AsyncHttpClient asyncClient;
    private OutputStream outputStream;
    private boolean https;
    private final EndpointAddress endpoint;
    private final Packet context;
    private CookieJar cookieJar = null;
    private final Integer chunkSize;
    private String method;

    public AsyncHttpTransport(@NotNull Packet packet, @NotNull Map<String,List<String>> reqHeaders) {
        endpoint = packet.endpointAddress;
        context = packet;
        this.reqHeaders = reqHeaders;
        chunkSize = (Integer)context.invocationProperties.get(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
    }

    /*
     * Prepare the stream for HTTP request
     */
    public OutputStream writeOutput(final Codec codec, final Packet p) {
        try {


            createHttpConnection();
            //TODO rk sendCookieAsNeeded();
            if (requiresOutputStream()) {
                requestBuilder.setBody(new Request.EntityWriter(){

                    public void writeEntity(OutputStream out) throws IOException {
                        if (chunkSize != null) {
                            out = new WSChunkedOuputStream(out, chunkSize);
                        }
                        Collection<String> contentEncoding = reqHeaders.get("Content-Encoding");
                        // TODO need to find out correct encoding based on q value - RFC 2616
                        if (contentEncoding != null && contentEncoding.iterator().next().contains("gzip")) {
                            outputStream = new GZIPOutputStream(outputStream);
                        }
                        codec.encode(p, out);
                    }
                });
            }

            Future<Response> responseF = asyncClient.executeRequest(requestBuilder.build());
            response= responseF.get();
        } catch (Exception ex) {
            throw new ClientTransportException(
                ClientMessages.localizableHTTP_CLIENT_FAILED(ex),ex);
        }

        return outputStream;
    }

    /*
     * Prepare the stream for HTTP request
     */
    public OutputStream writeOutput(ByteArrayBuffer b) {
        try {


            createHttpConnection();
            //TODO rk sendCookieAsNeeded();
            if (requiresOutputStream()) {
                requestBuilder.setBody(b.newInputStream());
            }

            Future<Response> responseF = asyncClient.executeRequest(requestBuilder.build());
            response= responseF.get();
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
        // response processing

        InputStream in;
        try {
            //TODO rk saveCookieAsNeeded();
            in = readResponse();
            if (in != null) {
                String contentEncoding = null;
                /*TODO rk
                contentEncoding = httpConnection.getContentEncoding();
                */
                if (contentEncoding != null && contentEncoding.contains("gzip")) {
                    in = new GZIPInputStream(in);
                }
            }
        } catch (IOException e) {
            throw new ClientTransportException(ClientMessages.localizableHTTP_STATUS_CODE(statusCode, statusMessage), e);
        }
        return in;
    }

    public Map<String, List<String>> getHeaders() {
        if (respHeaders != null) {
            return respHeaders;
        }
        respHeaders = new Headers();
        //TODO rk
        //respHeaders.putAll(response.getHeaders().iterator());
        return respHeaders;
    }

    protected @Nullable InputStream readResponse() {
        InputStream is;
        try {
            is = response.getResponseBodyAsStream();
        } catch(IOException ioe) {
            throw new WebServiceException(ioe);
        }
        if (is == null) {
            return is;
        }
        /*TODO rk
        // Since StreamMessage doesn't read </s:Body></s:Envelope>, there
        // are some bytes left in the InputStream. This confuses JDK and may
        // not reuse underlying sockets. Hopefully JDK fixes it in its code !
        final InputStream temp = is;
        return new FilterInputStream(temp) {
            // Workaround for "SJSXP XMLStreamReader.next() closes stream".
            // So it doesn't read from the closed stream
            boolean closed;
            @Override
            public void close() throws IOException {
                if (!closed) {
                    closed = true;
                    byte[] buf = new byte[8192];
                    while(temp.read(buf) != -1);
                    super.close();
                }
            }
        };
        */
        return is;
    }


    protected void readResponseCodeAndMessage() {
            statusCode = response.getStatusCode();
            statusMessage = response.getStatusText();
     
    }
    /* todo rk
    protected void sendCookieAsNeeded() {
        Boolean shouldMaintainSessionProperty =
            (Boolean) context.invocationProperties.get(SESSION_MAINTAIN_PROPERTY);
        if (shouldMaintainSessionProperty != null && shouldMaintainSessionProperty) {
            cookieJar = (CookieJar) context.invocationProperties.get(HTTP_COOKIE_JAR);
            if (cookieJar == null) {
                cookieJar = new CookieJar();

                // need to store in binding's context so it is not lost
                context.proxy.getRequestContext().put(HTTP_COOKIE_JAR, cookieJar);
            }
            cookieJar.applyRelevantCookies(httpConnection);
        }
    }

    private void saveCookieAsNeeded() {
        if (cookieJar != null) {
            cookieJar.recordAnyCookies(httpConnection);
        }
    }
    */

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

        asyncClient = new AsyncHttpClient(asyncConfigBuilder.build());


        /*TODO
        Integer chunkSize = (Integer)context.invocationProperties.get(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
        if (chunkSize != null) {
            httpConnection.setChunkedStreamingMode(chunkSize);
        }
        */
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

    // overide default SSL HttpClientVerifier to always return true
    // effectively overiding Hostname client verification when using SSL
    private static class HttpClientVerifier implements HostnameVerifier {
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

    /**
     * HttpURLConnection.getOuputStream() returns sun.net.www.http.ChunkedOuputStream in chunked
     * streaming mode. If you call ChunkedOuputStream.write(byte[20MB], int, int), then the whole data
     * is kept in memory. This wraps the ChunkedOuputStream so that it writes only small
     * chunks.
     */
    private static final class WSChunkedOuputStream extends FilterOutputStream {
        final int chunkSize;

        WSChunkedOuputStream(OutputStream actual, int chunkSize) {
            super(actual);
            this.chunkSize = chunkSize;
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            while(len > 0) {
                int sent = (len > chunkSize) ? chunkSize : len;
                out.write(b, off, sent);        // don't use super.write() as it writes byte-by-byte
                len -= sent;
                off += sent;
            }
        }

    }

}

