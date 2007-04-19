package org.jvnet.jax_ws_commons.transport.smtp.server;

import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.TransportBackChannel;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.ExceptionHasMessage;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.server.UnsupportedMediaException;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.ws.WebServiceException;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.Enumeration;
import java.security.Principal;

import org.jvnet.jax_ws_commons.transport.smtp.mail.EmailEndpoint;
import org.jvnet.jax_ws_commons.transport.smtp.mail.MailHandler;

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

    private EmailEndpoint email;

    SMTPAdapter(WSEndpoint endpoint) {
        super(endpoint);
    }

    public void setEmailEndpoint(EmailEndpoint email) {
        this.email = email;
    }

    public EmailEndpoint getEmailEndpoint() {
        return email;
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
     private Packet decodePacket(@NotNull MimeMessage con, @NotNull Codec codec) throws MessagingException, IOException {
         String ct = con.getContentType();
         InputStream in = con.getInputStream();
         Packet packet = new Packet();
         packet.soapAction = con.getHeader("SOAPAction")[0];
         packet.wasTransportSecure = false;
         packet.acceptableMimeTypes = con.getHeader("Accept")[0];
         //packet.addSatellite(con);        TODO
         //packet.transportBackChannel = new Oneway(con);
         packet.webServiceContextDelegate = new WebServiceContextDelegate() {

             public Principal getUserPrincipal(@NotNull Packet request) {
                 return null;
             }

             public boolean isUserInRole(@NotNull Packet request, String role) {
                 return false;
             }

             @NotNull
             public String getEPRAddress(@NotNull Packet request, @NotNull WSEndpoint endpoint) {
                 return null;
             }

             @Nullable
             public String getWSDLAddress(@NotNull Packet request, @NotNull WSEndpoint endpoint) {
                 return null;
             }
         };

         if (dump) {
             ByteArrayBuffer buf = new ByteArrayBuffer();
             buf.write(in);
             dump(buf, "SMTP request", con.getAllHeaders());
             in = buf.newInputStream();
         }
         codec.decode(in, ct, packet);
         return packet;
     }

     private void encodePacket(@NotNull Packet packet, @NotNull Codec codec) throws MessagingException, IOException {
/*
         if (con.isClosed()) {
             return;                 // Connection is already closed
         }
*/
         Message responseMessage = packet.getMessage();
         if (responseMessage == null) {
/*
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
*/
         } else {
/*             if (con.getStatus() == 0) {
                 // if the appliation didn't set the status code,
                 // set the default one.
                 con.setStatus(responseMessage.isFault()
                         ? HttpURLConnection.HTTP_INTERNAL_ERROR
                         : HttpURLConnection.HTTP_OK);
             }*/

             ContentType contentType = codec.getStaticContentType(packet);
             if (contentType != null) {
/*                 con.setContentTypeResponseHeader(contentType.getContentType());
                 OutputStream os = con.getOutput();
                 if (dump) {
                     ByteArrayBuffer buf = new ByteArrayBuffer();
                     codec.encode(packet, buf);
                     //dump(buf, "SMTP response " + con.getStatus(), con.getResponseHeaders());
                     buf.writeTo(os);
                 } else {
                     codec.encode(packet, os);
                 }
                 os.close();*/

                 ByteArrayBuffer buf = new ByteArrayBuffer();
                 codec.encode(packet, buf);
                 MimeMessage msg = new MimeMessage(email.getSession());
                 msg.setContent(new String(buf.toByteArray()), contentType.getContentType());
                 email.send(msg);
             } else {
/*                 ByteArrayBuffer buf = new ByteArrayBuffer();
                 contentType = codec.encode(packet, buf);
                 con.setContentTypeResponseHeader(contentType.getContentType());
                 if (dump) {
                     //dump(buf, "HTTP response " + con.getStatus(), con.getResponseHeaders());
                 }
                 OutputStream os = con.getOutput();
                 buf.writeTo(os);
                 os.close();*/
             }
         }
     }

     public void invokeAsync(final MimeMessage con) throws IOException {
         final SMTPToolkit tk = pool.take();
         final Packet request;
         try {
             request = decodePacket(con, tk.codec);
         } catch(ExceptionHasMessage e) {
             LOGGER.log(Level.SEVERE, e.getMessage(), e);
             Packet response = new Packet();
             response.setMessage(e.getFaultMessage());
             try {
                encodePacket(response, tk.codec);
             } catch(MessagingException me) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
             }
             pool.recycle(tk);
             //con.close();
             return;
         } catch(UnsupportedMediaException e) {
             LOGGER.log(Level.SEVERE, e.getMessage(), e);
             Packet response = new Packet();
             //con.setStatus(WSHTTPConnection.UNSUPPORTED_MEDIA);
             //encodePacket(response, tk.codec);
             pool.recycle(tk);
             //con.close();
             return;
         } catch(MessagingException e) {
             LOGGER.log(Level.SEVERE, e.getMessage(), e);
             return;
         }

         endpoint.schedule(request, new WSEndpoint.CompletionCallback() {
             public void onCompletion(@NotNull Packet response) {
                 try {
                     try {
                         encodePacket(response, tk.codec);
                     } catch(IOException ioe) {
                         LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
                     } catch(MessagingException me) {
                        LOGGER.log(Level.SEVERE, me.getMessage(), me);
                     }
                     pool.recycle(tk);
                 } finally{
                     //con.close();
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

    public class SmtpHandler implements MailHandler {
        final Packet request;

        SmtpHandler(Packet request) {
            this.request = request;
        }

        public void onNewMail(MimeMessage message) {
            try {
                invokeAsync(message);
            } catch(IOException ioe) {
                LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
            }
        }

        public boolean isClosed() {
            return false;
        }
    }


    private void dump(ByteArrayBuffer buf, String caption, Enumeration headers) throws IOException {
        /*
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
        */
    }


}
