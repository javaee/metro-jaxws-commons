package org.jvnet.jax_ws_commons.transport.smtp.client;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.*;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.util.ByteArrayBuffer;
import org.jvnet.jax_ws_commons.transport.smtp.mail.EmailEndpoint;
import org.jvnet.jax_ws_commons.transport.smtp.mail.MailHandler;
import org.jvnet.jax_ws_commons.transport.smtp.SMTPFeature;

import javax.mail.MessagingException;
import javax.mail.Message;
import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.xml.ws.WebServiceException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * SMTP transport tube.
 *
 * @author Vivek Pandey
 * @author Jitendra Kotamraju
 */
public class SMTPTransportTube extends AbstractTubeImpl {

    private static final Logger LOGGER = Logger.getLogger(SMTPTransportTube.class.getName());

    private final Codec codec;
    private final EmailEndpoint endpoint;
    private final SmtpHandler incomingHandler;

    public SMTPTransportTube(Codec codec, WSBinding binding, EndpointAddress endpointAddress) {
        this.codec = codec;
        SMTPFeature feature = binding.getFeature(SMTPFeature.class);
        if (feature != null) {
            this.endpoint = new EmailEndpoint(feature);
        } else {
            this.endpoint = new EmailEndpoint(endpointAddress.toString());
        }
        incomingHandler = new SmtpHandler();
        endpoint.setMailHandler(incomingHandler);
        this.endpoint.start();
    }

    public SMTPTransportTube(SMTPTransportTube that, TubeCloner cloner) {
        this.codec = that.codec.copy();
        this.endpoint = that.endpoint;
        this.incomingHandler = that.incomingHandler;
        cloner.add(this, that);
    }

    @NotNull
    public NextAction processRequest(@NotNull Packet request) {

        if (dump) {
            this.endpoint.enableLog();
        } 
        try {
            final ByteArrayBuffer buf = new ByteArrayBuffer();
            final ContentType ct = codec.encode(request, buf);
            MimeMessage msg = new MyMessage(endpoint.getSession());
            Address to = new InternetAddress(request.endpointAddress.getURI().getAuthority());
            msg.setRecipient(Message.RecipientType.TO, to);
            msg.setDataHandler(new DataHandler(new DataSource() {

                public InputStream getInputStream() throws IOException {
                    return buf.newInputStream();
                }

                public OutputStream getOutputStream() throws IOException {
                    return null;
                }

                public String getContentType() {
                    return ct.getContentType();
                }

                public String getName() {
                    return "";
                }
            }));

            UUID msgId = endpoint.send(msg);
            // TODO what happens when the message arrives before addHandler is called
            incomingHandler.addHandler(msgId, new MessageHandler(request));
        } catch (IOException e) {
            throw new WebServiceException(e);
        } catch (MessagingException e) {
            throw new WebServiceException(e);
        }
        return doSuspend();
    }

    @NotNull
    public NextAction processResponse(@NotNull Packet response) {
        return doReturnWith(response);
    }

    @NotNull
    public NextAction processException(@NotNull Throwable t) {
        return doThrow(t);
    }

    public void preDestroy() {
        //Nothing to do?
    }

    public SMTPTransportTube copy(TubeCloner cloner) {
        return new SMTPTransportTube(this, cloner);
    }

    public class MessageHandler implements MailHandler {
        final Fiber fiber;
        final Packet request;

        MessageHandler(Packet request) {
            this.request = request;
            fiber = Fiber.current();
        }

        public void onNewMail(MimeMessage message) {
            Packet reply = request.createClientResponse(null);
            try {
                codec.decode(message.getRawInputStream(), message.getContentType(), reply);
            } catch (Exception e) {
                e.printStackTrace();
            }

            fiber.resume(reply);
        }
    }

    public class SmtpHandler implements MailHandler {
        Map<UUID, MailHandler> waiting = Collections.synchronizedMap(new HashMap<UUID, MailHandler>());

        public void addHandler(UUID msgId, MailHandler handler ) {
            waiting.put(msgId, handler);
        }

        public void onNewMail(MimeMessage message) {
            UUID key = EmailEndpoint.getKey(message);
            if (key != null) {
                MailHandler cbak = waiting.remove(key);
                if (cbak == null) {
                    LOGGER.warning("Received unexpected message with key = "+key);
                } else {
                    cbak.onNewMail(message);
                }
            } else {
                LOGGER.warning("Received unexpected message - cannot find key");
            }
        }
    }

    /*
     * So that javamail doesn't override our Message-ID
     */
    private static class MyMessage extends MimeMessage  {

        public MyMessage(Session session) {
            super(session);
        }

        @Override
        protected void updateMessageID() throws MessagingException {
        }

    }

    /**
     * Dumps what goes across SMTP transport.
     */
    public static boolean dump;

    static {
        boolean b;
        try {
            b = Boolean.getBoolean(SMTPTransportTube.class.getName() + ".dump");
        } catch (Throwable t) {
            b = false;
        }
        dump = b;
    }

}
