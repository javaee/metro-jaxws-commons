package org.jvnet.jax_ws_commons.json;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * author Jitendra Kotamraju
 */
class JSONCodec implements Codec {

    private static final String JSON_MIME_TYPE = "application/json";
    private static final ContentType jsonContentType = new JSONContentType();
    private static final Map<String, String> nstojns;
    static {
        Map<String, String> tmp = new HashMap<String, String>();
        tmp.put( "http://schemas.xmlsoap.org/soap/envelope/", "s");
        tmp.put( "http://jax-ws.dev.java.net/json", "tns");
        nstojns = Collections.unmodifiableMap(tmp);
    }

    // TODO Thread-safe ??
    private static final XMLOutputFactory xof = new MappedXMLOutputFactory(nstojns);
    private static final XMLInputFactory xif = new MappedXMLInputFactory(nstojns);


    private final WSBinding binding;
    private final SOAPVersion soapVersion;

    public JSONCodec(WSBinding binding) {
        this.binding = binding;
        this.soapVersion = binding.getSOAPVersion();
    }

    public String getMimeType() {
        return JSON_MIME_TYPE;
    }

    public ContentType getStaticContentType(Packet packet) {
        return jsonContentType;
    }

    public ContentType encode(Packet packet, OutputStream out) throws IOException {
        Message message = packet.getMessage();
        if (message != null) {
            XMLStreamWriter sw = null;
            try {
                sw = xof.createXMLStreamWriter(out);
                sw.writeStartDocument();
                message.writePayloadTo(sw);
                sw.writeEndDocument();
            } catch(XMLStreamException xe) {
                throw new WebServiceException(xe);
            } finally {
                if (sw != null) {
                    try {
                        sw.close();
                    } catch(XMLStreamException xe) {
                        throw new WebServiceException(xe);
                    }
                }
            }
        }
        return jsonContentType;
    }

    public ContentType encode(Packet packet, WritableByteChannel buffer) {
        throw new UnsupportedOperationException();
    }

    public Codec copy() {
        return new JSONCodec(binding);
    }

    public void decode(InputStream in, String contentType, Packet response) throws IOException {
        in = JSONCodec.hasSomeData(in);
        Message message;
        if (in == null) {
            message = Messages.createEmpty(soapVersion);
        } else {
            XMLStreamReader reader;
            try {
                reader = xif.createXMLStreamReader(in);
            } catch(XMLStreamException xe) {
                throw new WebServiceException(xe);
            }
            message = Messages.createUsingPayload(reader, soapVersion);
        }
        response.setMessage(message);
    }

    public void decode(ReadableByteChannel in, String contentType, Packet response) {
        throw new UnsupportedOperationException();
    }

    private static final class  JSONContentType implements ContentType {

        private static final String JSON_CONTENT_TYPE = JSON_MIME_TYPE;

        public String getContentType() {
            return JSON_CONTENT_TYPE;
        }

        public String getSOAPActionHeader() {
            return null;
        }

        public String getAcceptHeader() {
            return JSON_CONTENT_TYPE;
        }

    }

    /**
     * Finds if the stream has some content or not
     * @param in input content
     * @return null if there is no data
     *         else stream to be used
     * @throws IOException if any I/O error happens
     */
    private static InputStream hasSomeData(InputStream in) throws IOException {
        if (in != null) {
            if (in.available() < 1) {
                if (!in.markSupported()) {
                    in = new BufferedInputStream(in);
                }
                in.mark(1);
                if (in.read() != -1) {
                    in.reset();
                } else {
                    in = null;          // No data
                }
            }
        }
        return in;
    }

}
