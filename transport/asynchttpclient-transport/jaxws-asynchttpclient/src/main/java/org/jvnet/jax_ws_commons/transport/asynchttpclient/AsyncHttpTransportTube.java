package org.jvnet.jax_ws_commons.transport.asynchttpclient;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.*;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.AsyncProviderCallback;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.resources.ClientMessages;
import com.sun.xml.ws.transport.Headers;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.util.ByteArrayBuffer;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Jitendra Kotamraju
 * @author Rama Pulavarthi
 */
public class AsyncHttpTransportTube extends AbstractTubeImpl {
    private final Codec codec;
    private final WSBinding binding;

    public AsyncHttpTransportTube(Codec codec, WSBinding binding) {
        this.codec = codec;
        this.binding = binding;
    }
    /**
     * Copy constructor for {@link com.sun.xml.ws.api.pipe.Tube#copy(TubeCloner)}.
     */
    private AsyncHttpTransportTube(AsyncHttpTransportTube that, TubeCloner cloner) {
        this( that.codec.copy(), that.binding);
        cloner.add(that,this);
    }

    public NextAction processException(@NotNull Throwable t) {
        throw new IllegalStateException("HttpTransportPipe's processException shouldn't be called.");
    }

    public NextAction processRequest(@NotNull Packet request) {
        AsyncHttpTransport con;
        try {
            // get transport headers from message
            Map<String, List<String>> reqHeaders = new Headers();
            Map<String, List<String>> userHeaders = (Map<String, List<String>>) request.invocationProperties.get(MessageContext.HTTP_REQUEST_HEADERS);
            if (userHeaders != null) {
                // userHeaders may not be modifiable like SingletonMap, just copy them
                reqHeaders.putAll(userHeaders);
            }

            con = new AsyncHttpTransport(request,reqHeaders);
            AsyncCallbackImpl callback = new AsyncCallbackImpl(request, con);
            con.setCallback(callback);
            //TODO rk request.addSatellite(new HttpResponseProperties(con));

            ContentType ct = codec.getStaticContentType(request);
            if (ct == null) {
                ByteArrayBuffer buf = new ByteArrayBuffer();

                ct = codec.encode(request, buf);
                // data size is available, set it as Content-Length
                reqHeaders.put("Content-Length", Collections.singletonList(Integer.toString(buf.size())));
                reqHeaders.put("Content-Type", Collections.singletonList(ct.getContentType()));
                if (ct.getAcceptHeader() != null) {
                    reqHeaders.put("Accept", Collections.singletonList(ct.getAcceptHeader()));
                }
                if (binding instanceof SOAPBinding) {
                    writeSOAPAction(reqHeaders, ct.getSOAPActionHeader(),request);
                }

                if(dump)
                    dump(buf, "HTTP request", reqHeaders);

                con.writeOutput(buf);
            } else {
                // Set static Content-Type
                reqHeaders.put("Content-Type", Collections.singletonList(ct.getContentType()));
                if (ct.getAcceptHeader() != null) {
                    reqHeaders.put("Accept", Collections.singletonList(ct.getAcceptHeader()));
                }
                if (binding instanceof SOAPBinding) {
                    writeSOAPAction(reqHeaders, ct.getSOAPActionHeader(), request);
                }

                if(dump) {
                    ByteArrayBuffer buf = new ByteArrayBuffer();
                    codec.encode(request, buf);
                    dump(buf, "HTTP request - "+request.endpointAddress, reqHeaders);
                    con.writeOutput(buf);
                } else {
                    con.writeOutput(codec,request);
                }
            }

            con.closeOutput();
        } catch(WebServiceException wex) {
            throw wex;
        } catch(Exception ex) {
            throw new WebServiceException(ex);
        }
        return doSuspend();
    }

    public class AsyncCallbackImpl {
        private final Packet request;
        private final Fiber fiber;
        private final AsyncHttpTransport con;

        public AsyncCallbackImpl(Packet request, AsyncHttpTransport con) {
            this.request = request;
            this.fiber = Fiber.current();
            this.con = con;
        }

        public void send() {
            Packet packet;
            try {
                con.readResponseCodeAndMessage();   // throws IOE
                InputStream response = con.getInput();
                if(dump) {
                    ByteArrayBuffer buf = new ByteArrayBuffer();
                    if (response != null) {
                        buf.write(response);
                        response.close();
                    }
                    dump(buf,"HTTP response - "+request.endpointAddress+" - "+con.statusCode, con.getHeaders());
                    response = buf.newInputStream();
                }

                if (con.statusCode== WSHTTPConnection.ONEWAY || (request.expectReply != null && !request.expectReply)) {
                    checkStatusCodeOneway(response, con.statusCode, con.statusMessage);   // throws ClientTransportException
                    packet = request.createClientResponse(null);    // one way. no response given.
                    fiber.resume(packet);
                }

                checkStatusCode(response, con.statusCode, con.statusMessage); // throws ClientTransportException

                String contentType = con.getContentType();
                // TODO check if returned MIME type is the same as that which was sent
                // or is acceptable if an Accept header was used
                packet = request.createClientResponse(null);
                //reply.addSatellite(new HttpResponseProperties(con));
                packet.wasTransportSecure = con.isSecure();
                codec.decode(response, contentType, packet);

            } catch(WebServiceException wex) {
                throw wex;
            } catch(Exception ex) {
                throw new WebServiceException(ex);
            }
            
            fiber.resume(packet);
        }

        public void sendError(@NotNull Throwable t) {
            //fiber.resume(packet);
        }
    }


    public NextAction processResponse(@NotNull Packet response) {
        return doReturnWith(response);
    }

    private void checkStatusCode(InputStream in, int statusCode, String statusMessage) throws IOException {
        // SOAP1.1 and SOAP1.2 differ here
        if (binding instanceof SOAPBinding) {
            if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_INTERNAL_ERROR) {
                if (in != null) {
                    in.close();
                }
                throw new ClientTransportException(ClientMessages.localizableHTTP_STATUS_CODE(statusCode, statusMessage));
            }
        }
        // Every status code is OK for XML/HTTP
    }

    private void checkStatusCodeOneway(InputStream in, int statusCode, String statusMessage) throws IOException {
        if (statusCode != WSHTTPConnection.ONEWAY && statusCode != WSHTTPConnection.OK) {
            if (in != null) {
                in.close();
            }
            throw new ClientTransportException(ClientMessages.localizableHTTP_STATUS_CODE(statusCode,statusMessage));
        }
    }

    /**
     * write SOAPAction header if the soapAction parameter is non-null or BindingProvider properties set.
     * BindingProvider properties take precedence.
     */
    private void writeSOAPAction(Map<String, List<String>> reqHeaders, String soapAction, Packet packet) {
        //dont write SOAPAction HTTP header for SOAP 1.2 messages.
        if(SOAPVersion.SOAP_12.equals(binding.getSOAPVersion()))
            return;
        if (soapAction != null)
            reqHeaders.put("SOAPAction", Collections.singletonList(soapAction));
        else
            reqHeaders.put("SOAPAction", Collections.singletonList("\"\""));
    }

    public void preDestroy() {
        // nothing to do. Intentionally left empty.
    }

    @Override
    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new AsyncHttpTransportTube(this, cloner);
    }

    private void dump(ByteArrayBuffer buf, String caption, Map<String, List<String>> headers) throws IOException {
        System.out.println("---["+caption +"]---");
        for (Map.Entry<String,List<String>> header : headers.entrySet()) {
            if(header.getValue().isEmpty()) {
                // I don't think this is legal, but let's just dump it,
                // as the point of the dump is to uncover problems.
                System.out.println(header.getValue());
            } else {
                for (String value : header.getValue()) {
                    System.out.println(header.getKey()+": "+value);
                }
            }
        }

        buf.writeTo(System.out);
        System.out.println("--------------------");
    }
    
    /**
     * Dumps what goes across HTTP transport.
     */
    public static boolean dump;

    static {
        boolean b;
        try {
            b = Boolean.getBoolean(AsyncHttpTransportTube.class.getName()+".dump");
        } catch( Throwable t ) {
            b = false;
        }
        dump = b;
    }
}
