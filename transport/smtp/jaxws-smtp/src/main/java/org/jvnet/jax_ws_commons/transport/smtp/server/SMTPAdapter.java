package org.jvnet.jax_ws_commons.transport.smtp.server;

import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.TransportBackChannel;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.ExceptionHasMessage;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.server.UnsupportedMediaException;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.istack.NotNull;

import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;

/**
 * @author Jitendra Kotamraju
 */
public class SMTPAdapter extends Adapter<SMTPAdapter.SMTPToolkit> {

    /**
     * Dumps what goes across SMTP transport.
     */
    public static boolean dump = false;

    static {
        try {
            dump = Boolean.getBoolean(SMTPAdapter.class.getName()+".dump");
        } catch( Throwable t ) {
        }
    }

    private static final Logger LOGGER = Logger.getLogger(SMTPAdapter.class.getName());

    SMTPAdapter(WSEndpoint endpoint) {
        super(endpoint);
    }

    protected SMTPToolkit createToolkit() {
        return new SMTPToolkit();
    }

    /**
      *
      * @param con
      * @param codec
      * @return
      * @throws java.io.IOException
      *         ExceptionHasMessage exception that contains particular fault message
      *         UnsupportedMediaException to indicate to send 415 error code
      */
     private Packet decodePacket(@NotNull WSHTTPConnection con, @NotNull Codec codec) throws IOException {
         String ct = con.getRequestHeader("Content-Type");
         InputStream in = con.getInput();
         Packet packet = new Packet();
         packet.soapAction = con.getRequestHeader("SOAPAction");
         packet.wasTransportSecure = con.isSecure();
         packet.acceptableMimeTypes = con.getRequestHeader("Accept");
         packet.addSatellite(con);
         packet.transportBackChannel = new Oneway(con);
         packet.webServiceContextDelegate = con.getWebServiceContextDelegate();

         if (dump) {
             ByteArrayBuffer buf = new ByteArrayBuffer();
             buf.write(in);
             dump(buf, "SMTP request", con.getRequestHeaders());
             in = buf.newInputStream();
         }
         codec.decode(in, ct, packet);
         return packet;
     }

     private void encodePacket(@NotNull Packet packet, @NotNull WSHTTPConnection con, @NotNull Codec codec) throws IOException {
         if (con.isClosed()) {
             return;                 // Connection is already closed
         }
         Message responseMessage = packet.getMessage();
         if (responseMessage == null) {
             if (!con.isClosed()) {
                 // set the response code if not already set
                 // for example, 415 may have been set earlier for Unsupported Content-Type
                 if (con.getStatus() == 0)
                     con.setStatus(WSHTTPConnection.ONEWAY);
                 // close the response channel now
                 try {
                     con.getOutput().close(); // no payload
                 } catch (IOException e) {
                     throw new WebServiceException(e);
                 }
             }
         } else {
             if (con.getStatus() == 0) {
                 // if the appliation didn't set the status code,
                 // set the default one.
                 con.setStatus(responseMessage.isFault()
                         ? HttpURLConnection.HTTP_INTERNAL_ERROR
                         : HttpURLConnection.HTTP_OK);
             }

             ContentType contentType = codec.getStaticContentType(packet);
             if (contentType != null) {
                 con.setContentTypeResponseHeader(contentType.getContentType());
                 OutputStream os = con.getOutput();
                 if (dump) {
                     ByteArrayBuffer buf = new ByteArrayBuffer();
                     codec.encode(packet, buf);
                     dump(buf, "SMTP response " + con.getStatus(), con.getResponseHeaders());
                     buf.writeTo(os);
                 } else {
                     codec.encode(packet, os);
                 }
                 os.close();
             } else {
                 ByteArrayBuffer buf = new ByteArrayBuffer();
                 contentType = codec.encode(packet, buf);
                 con.setContentTypeResponseHeader(contentType.getContentType());
                 if (dump) {
                     dump(buf, "HTTP response " + con.getStatus(), con.getResponseHeaders());
                 }
                 OutputStream os = con.getOutput();
                 buf.writeTo(os);
                 os.close();
             }
         }
     }

     public void invokeAsync(final WSHTTPConnection con) throws IOException {
         final SMTPToolkit tk = pool.take();
         final Packet request;
         try {
             request = decodePacket(con, tk.codec);
         } catch(ExceptionHasMessage e) {
             LOGGER.log(Level.SEVERE, e.getMessage(), e);
             Packet response = new Packet();
             response.setMessage(e.getFaultMessage());
             encodePacket(response, con, tk.codec);
             pool.recycle(tk);
             con.close();
             return;
         } catch(UnsupportedMediaException e) {
             LOGGER.log(Level.SEVERE, e.getMessage(), e);
             Packet response = new Packet();
             con.setStatus(WSHTTPConnection.UNSUPPORTED_MEDIA);
             encodePacket(response, con, tk.codec);
             pool.recycle(tk);
             con.close();
             return;
         }

         endpoint.schedule(request, new WSEndpoint.CompletionCallback() {
             public void onCompletion(@NotNull Packet response) {
                 try {
                     try {
                         encodePacket(response, con, tk.codec);
                     } catch(IOException ioe) {
                         LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
                     }
                     pool.recycle(tk);
                 } finally{
                     con.close();
                 }
             }
         });
     }

    final class SMTPToolkit extends Adapter.Toolkit {
    }

    final class Oneway implements TransportBackChannel {
        WSHTTPConnection con;
        Oneway(WSHTTPConnection con) {
            this.con = con;
        }
        public void close() {
            if(!con.isClosed()) {
                // close the response channel now
                con.setStatus(WSHTTPConnection.ONEWAY);
                try {
                    con.getOutput().close(); // no payload
                } catch (IOException e) {
                    throw new WebServiceException(e);
                }
                con.close();
            }
        }
    }

    private void dump(ByteArrayBuffer buf, String caption, Map<String, List<String>> headers) throws IOException {
        System.out.println("---["+caption +"]---");
        if (headers != null) {
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                if (header.getValue().isEmpty()) {
                    // I don't think this is legal, but let's just dump it,
                    // as the point of the dump is to uncover problems.
                    System.out.println(header.getValue());
                } else {
                    for (String value : header.getValue()) {
                        System.out.println(header.getKey() + ": " + value);
                    }
                }
            }
        }
        buf.writeTo(System.out);
        System.out.println("--------------------");
    }


}
