package org.jvnet.jax_ws_commons.dime.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.jvnet.jax_ws_commons.dime.annotation.DimeOutput;
import org.jvnet.jax_ws_commons.dime.attachment.DimeMessage;
import org.jvnet.jax_ws_commons.dime.attachment.DimeRecord;
import org.jvnet.jax_ws_commons.dime.attachment.DimeRecord.TypeNameFormat;
import org.jvnet.jax_ws_commons.dime.binding.DimeBindingID;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.encoding.StreamSOAPCodec;
import com.sun.xml.ws.util.ByteArrayDataSource;


/**
 * A codec that can send and receive DIME encoded messages.
 * 
 * @author Oliver Treichel
 */
public class DimeCodec implements Codec {
    /** Mime type for the SOAP message part */
    private static final String MIME_CONTENT_TYPE = "text/xml";

    /** Actual SOAP 1.1 processing is passed a class in JAX-WS-RI */
    private final StreamSOAPCodec soapCodec = StreamSOAPCodec.create(SOAPVersion.SOAP_11);

    /**
     * Create a copy of this codec. This codec is stateless. Therefore, this method does not copy anything. It just
     * returns the current instance.
     * 
     * @see Codec#copy()
     */
    public Codec copy() {
        // this codec has no state
        return this;
    }

    /**
     * Decode an incoming DIME message. If the content type is not <code>application/dime</code>, fall back to plain
     * SOAP 1.1 over HTTP.
     * 
     * @see Codec#decode(InputStream, String, Packet)
     */
    public void decode(final InputStream in, final String contentType, final Packet packet) throws IOException {
        // TODO: check if the endpoint method has a DimeInput annotation
        if (DimeContentType.DIME_CONTENT_TYPE.equals(contentType)) {
            // decode DIME message
            final DimeMessage dimeMessage = new DimeMessage();
            dimeMessage.parseFrom(in);

            // parse SOAP message and resolve references to attachments
            final XMLStreamReader xmlReader = XMLStreamReaderFactory.create(null, dimeMessage.getSoapRecord().asInputStream(), true);
            final DimeStreamReader dimeReader = new DimeStreamReader(xmlReader, dimeMessage);
            packet.setMessage(soapCodec.decode(dimeReader, dimeMessage));
        } else {
            // not a DIME message - fall back to plain SOAP 1.1 over HTTP
            soapCodec.decode(in, contentType, packet);
        }
    }

    /**
     * Not implemented.
     * 
     * @see Codec#decode(ReadableByteChannel, String, Packet)
     */
    public void decode(final ReadableByteChannel in, final String contentType, final Packet response) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Encode an outgoing DIME message. The endpoint method should have a {@link DimeOutput} annotation to indicate that
     * is actually wants DIME format. Otherwise, the message will be sent as SOAP 1.1 over HTTP.
     * 
     * @see Codec#encode(Packet, OutputStream)
     */
    public ContentType encode(final Packet packet, final OutputStream out) throws IOException {
        // TODO: check if the endpoint method has a DimeOutput annotation
        if (false) {
            // send as normal SOAP 1.1 over HTTP due to lack of DimeOutput annotation
            return soapCodec.encode(packet, out);
        }

        try {
            // Construct the SOAP message part - it is small, so we can keep it in memory
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final XMLStreamWriter xmlWriter = XMLStreamWriterFactory.create(baos);
            final DimeMessage dimeMessage = new DimeMessage();
            final DimeStreamWriter dimeWriter = new DimeStreamWriter(xmlWriter, dimeMessage);
            packet.getMessage().writeTo(dimeWriter);

            // this is a workaround until we known how to check for annotations on the endpoint method
            if (dimeMessage.isEmpty()) {
                // no attachments - send as normal SOAP 1.1 over HTTP
                return soapCodec.encode(packet, out);
            }

            // wrap the SOAP message part into a DIME record
            final DataSource source = new ByteArrayDataSource(baos.toByteArray(), MIME_CONTENT_TYPE);
            final DataHandler handler = new DataHandler(source);
            final DimeRecord soapRecord = new DimeRecord();
            soapRecord.setFirst(true);
            soapRecord.setContentId("uuid:" + UUID.randomUUID().toString());
            soapRecord.setTypeNameFormat(TypeNameFormat.URI);
            soapRecord.setContentType("http://schemas.xmlsoap.org/soap/envelope/");
            soapRecord.setData(handler);
            dimeMessage.setSoapRecord(soapRecord);

            dimeMessage.writeTo(out);
        } catch (final XMLStreamException e) {
            throw new IOException(e);
        }

        return DimeContentType.getInstance();
    }

    /**
     * Not implemented.
     * 
     * @see Codec#encode(Packet, WritableByteChannel)
     */
    public ContentType encode(final Packet packet, final WritableByteChannel buffer) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * @see Codec#getMimeType()
     */
    public String getMimeType() {
        // TODO: is this correct?
        return DimeBindingID.DIME_BINDING;
    }

    /**
     * @see Codec#getStaticContentType(Packet)
     */
    public ContentType getStaticContentType(final Packet packet) {
        // content type is set via return value of encode()
        return null;
    }
}
