package org.jvnet.jax_ws_commons.dime.codec;

import java.io.OutputStream;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jvnet.jax_ws_commons.dime.attachment.DimeMessage;
import org.jvnet.jax_ws_commons.dime.attachment.DimeRecord;
import org.jvnet.jax_ws_commons.dime.attachment.DimeRecord.TypeNameFormat;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.NamespaceContextEx;
import org.jvnet.staxex.XMLStreamWriterEx;

import com.sun.xml.ws.util.ByteArrayDataSource;
import com.sun.xml.ws.util.xml.XMLStreamWriterFilter;


/**
 * Write SOAP message and replace binary data with references to attachments. This class is a filter for a standard
 * {@link XMLStreamWriter} and adds custom behavior for attachments.
 * <p>
 * Attachment data in the SOAP message will be replaced by a <code>href</code> attribute who's value matches the
 * content id of an attachment in the DIME message.
 * </p>
 * 
 * @author Oliver Treichel
 */
public class DimeStreamWriter extends XMLStreamWriterFilter implements XMLStreamWriterEx {
    /** Holder for the binary attachments. */
    private final DimeMessage dimeMessage;

    /**
     * Constructor for the writer.
     * 
     * @param writer
     *            The standard {@link XMLStreamWriter}, that backs this implementation.
     * @param dimeMessage
     *            The holder for the binary attachments.
     */
    public DimeStreamWriter(final XMLStreamWriter writer, final DimeMessage dimeMessage) {
        super(writer);
        this.dimeMessage = dimeMessage;
    }

    /**
     * see {@link XMLStreamWriterEx#getNamespaceContext()}
     */
    @Override
    public NamespaceContextEx getNamespaceContext() {
        return new DimeNamespaceContextEx(super.getNamespaceContext());
    }

    /**
     * Write binary attachment data from a byte array.
     * 
     * @see XMLStreamWriterEx#writeBinary(byte[], int, int, String)
     */
    public void writeBinary(final byte[] data, final int start, final int len, final String contentType) throws XMLStreamException {
        writeBinary(new DataHandler(new ByteArrayDataSource(data, start, len, contentType)));
    }

    /**
     * Write binary attachment data from a {@link DataHandler}.
     * 
     * @see XMLStreamWriterEx#writeBinary(DataHandler)
     */
    public void writeBinary(final DataHandler data) throws XMLStreamException {
        // generate a unique id for the attachment
        final String id = "uuid:" + UUID.randomUUID().toString();
        final DimeRecord dimeRecord = new DimeRecord();
        dimeRecord.setContentId(id);
        dimeRecord.setData(data);
        if (data.getContentType() != null) {
            dimeRecord.setTypeNameFormat(TypeNameFormat.MEDIA_TYPE);
            dimeRecord.setContentType(data.getContentType());
        }
        dimeMessage.add(dimeRecord);

        // write a reference to the SOAP message
        writeAttribute("href", id);
    }

    /**
     * Not implemented.
     * 
     * @see XMLStreamWriterEx#writeBinary(String)
     */
    public OutputStream writeBinary(final String contentType) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    /**
     * Write binary attachment data from a {@link CharSequence}.
     * 
     * @see XMLStreamWriterEx#writePCDATA(CharSequence)
     */
    public void writePCDATA(final CharSequence data) throws XMLStreamException {
        if (data == null) {
            return;
        }
        if (data instanceof Base64Data) {
            final Base64Data binaryData = (Base64Data) data;
            writeBinary(binaryData.getDataHandler());
            return;
        }
        writeCharacters(data.toString());
    }
}
