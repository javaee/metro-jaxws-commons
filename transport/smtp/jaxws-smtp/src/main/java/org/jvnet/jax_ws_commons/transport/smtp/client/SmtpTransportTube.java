package org.jvnet.jax_ws_commons.transport.smtp.client;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.*;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.util.ByteArrayBuffer;
import org.jvnet.jax_ws_commons.transport.smtp.mail.EmailEndpoint;
import org.jvnet.jax_ws_commons.transport.smtp.mail.MailHandler;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.xml.ws.WebServiceException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * SMTP transport tube.
 *
 * @author Vivek Pandey
 * @author Jitendra Kotamraju
 */
public class SmtpTransportTube extends AbstractTubeImpl {
    private final Codec codec;
    private final EmailEndpoint endpoint;


    public SmtpTransportTube(Codec codec, EndpointAddress endpointAddress) {
        this.codec = codec;
        this.endpoint = new EmailEndpoint(endpointAddress.toString());
        if (dump)
            this.endpoint.enableLog();
        this.endpoint.start();
    }

    public SmtpTransportTube(SmtpTransportTube that, TubeCloner cloner) {
        this.codec = that.codec.copy();
        this.endpoint = that.endpoint;
        cloner.add(this, that);
    }

    @NotNull
    public NextAction processRequest(@NotNull Packet request) {
        endpoint.setMailHandler(new SmtpHandler(request));

        try {
            final ByteArrayBuffer buf = new ByteArrayBuffer();
            final ContentType ct = codec.encode(request, buf);
            MimeMessage msg = new MimeMessage(endpoint.getSession());
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

            endpoint.send(msg);
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

    public SmtpTransportTube copy(TubeCloner cloner) {
        return new SmtpTransportTube(this, cloner);
    }

    public class SmtpHandler implements MailHandler {
        final Fiber fiber;
        final Packet request;

        SmtpHandler(Packet request) {
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

    /**
     * Dumps what goes across SMTP transport.
     */
    private static final boolean dump;

    static {
        boolean b;
        try {
            b = Boolean.getBoolean(SmtpTransportTube.class.getName() + ".dump");
        } catch (Throwable t) {
            b = false;
        }
        dump = b;
    }


}
