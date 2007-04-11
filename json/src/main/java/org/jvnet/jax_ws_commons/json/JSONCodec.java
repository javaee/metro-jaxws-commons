package org.jvnet.jax_ws_commons.json;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.server.EndpointAwareCodec;
import com.sun.xml.ws.api.server.WSEndpoint;
import org.json.JSONException;
import org.json.JSONTokener;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;


/**
 * Server-side {@link Codec} that generates JSON. 
 *
 * @author Jitendra Kotamraju
 */
class JSONCodec implements EndpointAwareCodec {

    private static final String JSON_MIME_TYPE = "application/json";
    private static final ContentType jsonContentType = new JSONContentType();

    private final WSBinding binding;
    private final SOAPVersion soapVersion;

    private SchemaInfo schemaInfo;

    public JSONCodec(WSBinding binding) {
        this(binding,null);
    }

    public JSONCodec(WSBinding binding, SchemaInfo schemaInfo) {
        this.binding = binding;
        this.soapVersion = binding.getSOAPVersion();
        this.schemaInfo = schemaInfo;
    }

    public void setEndpoint(WSEndpoint endpoint) {
        schemaInfo = new SchemaInfo(endpoint);
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
                sw = checkSchemaInfo().createXMLStreamWriter(new OutputStreamWriter(out,"UTF-8"));
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
                        // let the original exception get through
                    }
                }
            }
        }
        return jsonContentType;
    }

    public ContentType encode(Packet packet, WritableByteChannel buffer) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the up-to-date {@link SchemaInfo} for the current endpoint,
     * by either using a cache or by parsing new.
     */
    private SchemaInfo checkSchemaInfo() {
        if(schemaInfo==null)
            throw new IllegalStateException("JSON binding is only available for the server");
        return schemaInfo;
    }

    public Codec copy() {
        return new JSONCodec(binding,schemaInfo);
    }

    public void decode(InputStream in, String contentType, Packet response) throws IOException {
        in = JSONCodec.hasSomeData(in);
        Message message;
        if (in == null) {
            message = Messages.createEmpty(soapVersion);
        } else {
            XMLStreamReader reader;
            try {
                StringWriter sw = new StringWriter();
                // TODO: RFC-4627 calls for BOM check
                // TODO: honor charset sub header.
                Reader r = new InputStreamReader(in,"UTF-8");
                char[] buf = new char[1024];
                int len;
                while((len=r.read(buf))>=0)
                    sw.write(buf,0,len);
                r.close();

                reader = checkSchemaInfo().createXMLStreamReader(new JSONTokener(sw.toString()));
            } catch(XMLStreamException e) {
                throw new WebServiceException(e);
            } catch (JSONException e) {
                throw new WebServiceException(e);
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
