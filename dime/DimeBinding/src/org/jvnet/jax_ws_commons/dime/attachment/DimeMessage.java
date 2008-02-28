package org.jvnet.jax_ws_commons.dime.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.AttachmentSet;

/**
 * AttachmentSet implementation for DIME encoded Attachments. Each attachment is of type {@link DimeRecord}.
 * 
 * @author Oliver Treichel
 */
public class DimeMessage implements AttachmentSet {
    private static final long serialVersionUID = 1L;
    private final Map<String, Attachment> attachmentRecords = new HashMap<String, Attachment>();
    private DimeRecord soapRecord = null;

    /**
     * Add an attachment to this set,
     * 
     * @see AttachmentSet#add(Attachment)
     */
    public void add(final Attachment att) {
        attachmentRecords.put(att.getContentId(), att);
    }

    /**
     * Gets the attachment by the content ID.
     * 
     * @see AttachmentSet#get(String)
     */
    public Attachment get(final String contentId) {
        return attachmentRecords.get(contentId);
    }

    /**
     * Get the {@link DimeRecord} that holds the SOAP message (i.e. the first record).
     * 
     * @return The {@link DimeRecord} that holds the SOAP message.
     */
    public DimeRecord getSoapRecord() {
        return soapRecord;
    }

    /**
     * Returns true if there's no attachment.
     * 
     * @see AttachmentSet#isEmpty()
     */
    public boolean isEmpty() {
        return attachmentRecords.isEmpty();
    }

    /**
     * see {@link Iterable#iterator()}
     */
    public Iterator<Attachment> iterator() {
        return attachmentRecords.values().iterator();
    }

    /**
     * Read all DimeRecords from an InputSteam.
     * 
     * @param is
     * @throws IOException
     */
    public void parseFrom(final InputStream is) throws IOException {
        // the first record is the soap message
        soapRecord = DimeRecord.parseFrom(is);

        // all other records are attachment records
        while (true) {
            final DimeRecord r = DimeRecord.parseFrom(is);
            if (r == null) {
                break;
            }

            add(r);
        }
    }

    /**
     * Set the {@link DimeRecord} that holds the SOAP message.
     * 
     * @param soapRecord
     */
    public void setSoapRecord(final DimeRecord soapRecord) {
        this.soapRecord = soapRecord;
    }

    /**
     * Write all DimeRecords to an OutputStream.
     * 
     * @param os
     * @throws IOException
     */
    public void writeTo(final OutputStream os) throws IOException {
        // the first record is the soap message
        soapRecord.setFirst(true);
        soapRecord.setLast(attachmentRecords.isEmpty());
        soapRecord.writeTo(os);

        // all other records are attachment records
        final Iterator<Attachment> iter = iterator();
        while (iter.hasNext()) {
            final DimeRecord r = (DimeRecord) iter.next();
            r.setFirst(false);
            r.setLast(!iter.hasNext());
            r.writeTo(os);
        }
    }
}
