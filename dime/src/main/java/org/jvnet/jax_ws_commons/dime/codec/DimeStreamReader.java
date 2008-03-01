package org.jvnet.jax_ws_commons.dime.codec;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jvnet.jax_ws_commons.dime.attachment.DimeMessage;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.NamespaceContextEx;
import org.jvnet.staxex.XMLStreamReaderEx;

import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.util.xml.XMLStreamReaderFilter;


/**
 * Parse a SOAP message into a data model and resolve references to attachments. This class is a filter for a standard
 * {@link XMLStreamReader} and adds custom behavior for attachments.
 * <p>
 * Any XML element with a <code>href</code> attribute who's value matches the content id of an attachment in the DIME
 * message, will be recognized.
 * </p>
 * 
 * @author Oliver Treichel
 */
public class DimeStreamReader extends XMLStreamReaderFilter implements XMLStreamReaderEx {
    /** Temporarily store attachments */
    private Base64Data base64AttData;

    /** Was a binary attachment detected in the SOAP message? */
    private boolean dimeReferencePresent = false;

    /** Holder for the binary attachments. */
    private final DimeMessage message;

    /**
     * Constructor for the reader.
     * 
     * @param reader
     *            The standard {@link XMLStreamReader}, that backs this implementation.
     * @param message
     *            The holder for the binary attachments.
     */
    public DimeStreamReader(final XMLStreamReader reader, final DimeMessage message) {
        super(reader);
        this.message = message;
    }

    /**
     * @see XMLStreamReaderEx#getElementTextTrim()
     */
    public String getElementTextTrim() throws XMLStreamException {
        return super.getElementText().trim();
    }

    /**
     * For every {@link XMLStreamConstants#START_ELEMENT} event, check if the element has a <code>href</code>
     * attribute with a value that machtes the content id of one of the attachments.
     * 
     * @see XMLStreamReader#getEventType()
     */
    @Override
    public int getEventType() {
        final int eventType = super.getEventType();

        if (eventType == XMLStreamConstants.START_ELEMENT && !dimeReferencePresent) {
            // lookup attachment if the start element has a href with a matching id
            final String href = getAttributeValue(null, "href");
            if (href != null) {
                final Attachment att = message.get(href);
                if (att != null) {
                    base64AttData = new Base64Data();
                    base64AttData.set(att.asDataHandler());
                    dimeReferencePresent = true;
                }
            }
        } else if (eventType == XMLStreamConstants.END_ELEMENT) {
            // cleanup on end element
            dimeReferencePresent = false;
            base64AttData = null;
        }

        return eventType;
    }

    /**
     * see {@link XMLStreamReaderEx#getNamespaceContext()}
     */
    @Override
    public NamespaceContextEx getNamespaceContext() {
        return new DimeNamespaceContextEx(super.getNamespaceContext());
    }

    /**
     * If an attachment reference was detected, return it's data.
     * 
     * @see XMLStreamReaderEx#getPCDATA()
     */
    public CharSequence getPCDATA() throws XMLStreamException {
        if (dimeReferencePresent) {
            // get attachment as CharSequence
            dimeReferencePresent = false;
            return base64AttData;
        }
        return super.getText();
    }

    /**
     * If an attachment reference was detected, return it's data.
     * 
     * @see XMLStreamReader#getText()
     */
    @Override
    public String getText() {
        if (dimeReferencePresent) {
            // get attachment as string
            dimeReferencePresent = false;
            return base64AttData.toString();
        }
        return super.getText();
    }

    /**
     * If an attachment reference was detected, return it's data.
     * 
     * @see XMLStreamReader#getTextCharacters()
     */
    @Override
    public char[] getTextCharacters() {
        if (dimeReferencePresent) {
            // get attachment as array
            final char[] chars = new char[base64AttData.length()];
            base64AttData.writeTo(chars, 0);
            dimeReferencePresent = false;
            return chars;
        }
        return super.getTextCharacters();
    }

    /**
     * If an attachment reference was detected, return it's data.
     * 
     * @see XMLStreamReader#getTextCharacters(int, char[], int, int)
     */
    @Override
    public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length) throws XMLStreamException {
        if (dimeReferencePresent) {
            throw new UnsupportedOperationException();
        }

        return super.getTextCharacters(sourceStart, target, targetStart, length);
    }

    /**
     * If an attachment reference was detected, return it's length.
     * 
     * @see XMLStreamReader#getTextLength()
     */
    @Override
    public int getTextLength() {
        return dimeReferencePresent ? base64AttData.length() : super.getTextLength();
    }

    /**
     * If an attachment reference was detected, return it's offset.
     * 
     * @see XMLStreamReader#getTextStart()
     */
    @Override
    public int getTextStart() {
        return dimeReferencePresent ? 0 : super.getTextStart();
    }

    /**
     * If an attachment reference was detected, return it's data as the next event.
     * 
     * @see XMLStreamReader#next()
     */
    @Override
    public int next() throws XMLStreamException {
        return dimeReferencePresent ? XMLStreamConstants.CHARACTERS : super.next();
    }
}
