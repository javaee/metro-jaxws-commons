package org.jvnet.jax_ws_commons.dime.codec;

import com.sun.xml.ws.api.pipe.ContentType;

/**
 * A content type that represents a message in DIME format.
 * 
 * @author Oliver Treichel
 */
public class DimeContentType implements ContentType {
    /** Official mime type for dime messages. */
    public static final String DIME_CONTENT_TYPE = "application/dime";

    /** singleton instance */
    private static ContentType instance = null;

    /** Get the singleton. */
    public static ContentType getInstance() {
        if (instance == null) {
            instance = new DimeContentType();
        }

        return instance;
    }

    /**
     * private use only
     */
    private DimeContentType() {
        // disable instantiation
    }

    /**
     * We do not expect any SOAP headers.
     * 
     * @see ContentType#getAcceptHeader()
     */
    public String getAcceptHeader() {
        return null;
    }

    /**
     * This is DIME content type.
     * 
     * @see ContentType#getContentType()
     */
    public String getContentType() {
        return DIME_CONTENT_TYPE;
    }

    /**
     * We do not send SOAP action headers.
     * 
     * @see ContentType#getSOAPActionHeader()
     */
    public String getSOAPActionHeader() {
        return null;
    }
}
