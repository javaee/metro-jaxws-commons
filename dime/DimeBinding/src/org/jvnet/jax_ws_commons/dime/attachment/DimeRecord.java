package org.jvnet.jax_ws_commons.dime.attachment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;

import com.sun.xml.ws.api.message.Attachment;

/**
 * DIME encoded web service attachment.
 * 
 * @author Oliver Treichel
 */
public class DimeRecord implements Attachment {
    /**
     * Enumeration of known DIME type name formats. Determines the format of the records contentId.
     * 
     * @author Oliver Treichel
     */
    public enum TypeNameFormat {
        MEDIA_TYPE(1), NONE(4), UNCHANGED(0), UNKNOWN(3), URI(2);

        /**
         * Convert integer value to enum.
         * 
         * @param i
         * @return Enum who's value is i
         */
        public static TypeNameFormat get(final int i) {
            switch (i) {
            case 0:
                return UNCHANGED; // used for chunks
            case 1:
                return MEDIA_TYPE; // i.e. mime type
            case 2:
                return URI; // e.g. name space URI
            case 3:
                return UNKNOWN;
            case 4:
                return NONE;
            }

            throw new IllegalArgumentException("Unknown type name format!");
        }

        /** internal value */
        private final int value;

        /**
         * Private constructor.
         * 
         * @param value
         */
        private TypeNameFormat(final int value) {
            this.value = value;
        }

        /**
         * Convert enum to integer.
         * 
         * @return integer value if the type name format
         */
        public int value() {
            return value;
        }
    }

    /** supported protocol version */
    private static final byte VERSION = 1;

    /**
     * Read a new DimeRecord from an InputStream.
     * 
     * @param is
     * @return a dime record
     * @throws IOException
     */
    static DimeRecord parseFrom(final InputStream is) throws IOException {
        final DimeRecord r = new DimeRecord();

        int b = readInt8(is);
        if (b == -1) {
            // end of stream
            return null;
        }

        // check for supported protocol version
        if (b >> 3 != VERSION) {
            throw new IllegalArgumentException("Unsupported dime protocol version!");
        }

        // read flags
        r.setFirst((b & 4) == 4);
        r.setLast((b & 2) == 2);
        r.setChunked((b & 1) == 1);

        // can't be last and chunked at the same time
        assert !(r.isLast() && r.isChunked());

        // read type name format
        b = readInt8(is) >> 4;
        r.setTypeNameFormat(TypeNameFormat.get(b));

        // read length fields
        final int optionLen = readInt16(is);
        final int idLen = readInt16(is);
        final int typeLen = readInt16(is);
        int dataLen = readInt32(is);

        // read options - if any
        if (optionLen > 0) {
            final byte[] options = new byte[optionLen];
            is.read(options);
            r.setOptions(options);
            readPad(is, optionLen);
        }

        // read id - if any
        if (idLen > 0) {
            final byte[] id = new byte[idLen];
            is.read(id);
            r.setContentId(new String(id));
            readPad(is, idLen);
        }

        // read type - if any
        if (typeLen > 0) {
            final byte[] type = new byte[typeLen];
            is.read(type);
            r.setContentType(new String(type));
            readPad(is, typeLen);
        }

        // read data payload into temporary file
        final byte[] data = new byte[r.getChunkSize()];
        final File tempFile = new TempFile(File.createTempFile("dime", null));
        final FileOutputStream fos = new FileOutputStream(tempFile);
        int totalBytes = dataLen;
        while (totalBytes > 0) {
            final int bytesRead = is.read(data, 0, Math.min(r.getChunkSize(), totalBytes));
            fos.write(data, 0, bytesRead);
            totalBytes -= bytesRead;
        }
        readPad(is, dataLen);

        // read remaining chunks - if any
        while (r.isChunked()) {
            b = readInt8(is);
            if (b == -1) {
                throw new IllegalStateException("Unexpected end of stream!");
            }

            // check for supported protocol version
            if (b >> 3 != VERSION) {
                throw new IllegalArgumentException("Unsupported dime protocol version!");
            }

            // read flags
            // chunk can't be first
            assert !((b & 4) == 4);

            r.setLast((b & 2) == 2);
            r.setChunked((b & 1) == 1);

            // can't be last and chunked at the same time
            assert !(r.isLast() && r.isChunked());

            // read type name format - must be empty for chunks
            b = readInt8(is);
            assert b == 0;

            // read length fields - must be empty for chunks
            assert readInt16(is) == 0; // options length
            assert readInt16(is) == 0; // id length
            assert readInt16(is) == 0; // type length

            dataLen = readInt32(is);

            // read data payload into temporary file
            totalBytes = dataLen;
            while (totalBytes > 0) {
                final int bytesRead = is.read(data, 0, Math.min(r.getChunkSize(), totalBytes));
                fos.write(data, 0, bytesRead);
                totalBytes -= bytesRead;
            }
            readPad(is, dataLen);
        }

        fos.close();
        r.setData(new DataHandler(new FileDataSource(tempFile)));

        return r;
    }

    /**
     * Read a 16 bit integer.
     * 
     * @param is
     * @return a 16 bit integer
     * @throws IOException
     */
    private static int readInt16(final InputStream is) throws IOException {
        return readInt8(is) << 8 | readInt8(is);
    }

    /**
     * Read a 32 bit integer.
     * 
     * @param is
     * @return a 32 bit integer
     * @throws IOException
     */
    private static int readInt32(final InputStream is) throws IOException {
        return readInt16(is) << 16 | readInt16(is);
    }

    /**
     * Read a 8 bit integer.
     * 
     * @param is
     * @return a 8 bit integer
     * @throws IOException
     */
    private static int readInt8(final InputStream is) throws IOException {
        return is.read();
    }

    /**
     * Read padding up to the next 4 byte (32 bit) boundary.
     * 
     * @param is
     * @param len
     * @throws IOException
     */
    private static void readPad(final InputStream is, final int len) throws IOException {
        int count = len % 4;
        if (count != 0) {
            while (count++ < 4) {
                readInt8(is);
            }
        }
    }

    /** Does the record span multiple chunks? */
    private boolean chunked = false;

    /** Default chunk size. */
    private int chunkSize = 8192;

    /** The content id. */
    private String contentId = null;

    /** The content type. */
    private String contentType = null;

    /** Binary attachment data. */
    private DataHandler data = null;

    /** Is this the first record? */
    private boolean first = false;

    /** Is this the last record? */
    private boolean last = false;

    /** DIME options (unused) */
    private byte[] options = null;

    /** The format of the content type. */
    private TypeNameFormat typeNameFormat = TypeNameFormat.UNCHANGED;

    /**
     * Not implemented.
     * 
     * @throws UnsupportedOperationException
     * @see Attachment#asByteArray()
     */
    public byte[] asByteArray() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get this attachment as a {@link DataHandler}
     * 
     * @see Attachment#asDataHandler()
     */
    public DataHandler asDataHandler() {
        return data;
    }

    /**
     * Get this attachment as an InputStream.
     * 
     * @see Attachment#asInputStream()
     */
    public InputStream asInputStream() {
        try {
            return data.getInputStream();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Not implemented.
     * 
     * @throws UnsupportedOperationException
     * @see Attachment#asSource()
     */
    public Source asSource() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the size of the internal buffer for reading and writing data from the network to a file or vice versa.
     * Defaults to 8 kByte.
     * 
     * @return The size of the internal buffer in bytes.
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Gets the id of this attachment. The id is used as the value of the <code>href</code> attribute in the SOAP
     * message.
     * 
     * @return The content id.
     * @see Attachment#getContentId()
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * Return the mime type of the attachment if it is known (i.e if the DIME type format is
     * {@link TypeNameFormat#MEDIA_TYPE}.
     * 
     * @return The mime type or <code>null</code> if unknown.
     * @see Attachment#getContentType()
     */
    public String getContentType() {
        if (contentType == null && getTypeNameFormat() == TypeNameFormat.MEDIA_TYPE && asDataHandler() != null) {
            setContentType(asDataHandler().getContentType());
        }

        return contentType;
    }

    /**
     * Get the DIME options. This value is ignored by the implementation.
     * 
     * @return The options.
     */
    public byte[] getOptions() {
        return options;
    }

    /**
     * Get the DIME type name format.
     * 
     * @return The type name format.
     */
    public TypeNameFormat getTypeNameFormat() {
        return typeNameFormat;
    }

    /**
     * Is the data of the attachment chunked (i.e. is the content spread over multiple DIME records)? If the record is
     * chunked, there must be more records in the message (i.e. {@link #isLast()} must be <code>false</code>).
     * 
     * @return <code>true</code> if there is more data in the next record(s).
     */
    public boolean isChunked() {
        return chunked;
    }

    /**
     * Is this the first record in the DIME message? The first part of the DIME message is always the SOAP message.
     * 
     * @return <code>true</code> if this is the first record (i.e. the SOAP message), <code>false</code> if this
     *         record is an binary attachment.
     */
    public boolean isFirst() {
        return first;
    }

    /**
     * Is this the last record in the DIME message?
     * 
     * @return <code>true</code> if this is the last record in the DIME message.
     */
    public boolean isLast() {
        return last;
    }

    /**
     * Mark this record as chunked.
     * 
     * @param chunked
     */
    private void setChunked(final boolean chunked) {
        this.chunked = chunked;
    }

    /**
     * Sets the size of the internal buffer for reading and writing data from the network to a file or vice versa.
     * Defaults to 8 kByte.
     * 
     * @param chunkSize
     */
    public void setChunkSize(final int chunkSize) {
        this.chunkSize = chunkSize;
    }

    /**
     * Set the content id of the record. The id is used as the value of the <code>href</code> attribute in the SOAP
     * message.
     * 
     * @param id
     */
    public void setContentId(final String id) {
        this.contentId = id;
    }

    /**
     * Sets the content type of the record. The type name format should be set accordingly.
     * 
     * @param type
     * @see #setTypeNameFormat(DimeRecord.TypeNameFormat)
     */
    public void setContentType(final String type) {
        this.contentType = type;
    }

    /**
     * Sets the content of the attachment.
     * 
     * @param data
     */
    public void setData(final DataHandler data) {
        this.data = data;
    }

    /**
     * Mark this record as the fist record in the DIME message.
     * 
     * @param first
     */
    public void setFirst(final boolean first) {
        this.first = first;
    }

    /**
     * Mark this record as the last record in the DIME message.
     * 
     * @param last
     */
    public void setLast(final boolean last) {
        this.last = last;
    }

    /**
     * Set options to be send to the client.
     * 
     * @param options
     */
    public void setOptions(final byte[] options) {
        this.options = options;
    }

    /**
     * Set the type name format (i.e. the format of the content type).
     * 
     * @param typeFormat
     */
    public void setTypeNameFormat(final TypeNameFormat typeFormat) {
        this.typeNameFormat = typeFormat;
    }

    /**
     * Write 16 bit integer value.
     * 
     * @param os
     * @param i
     * @throws IOException
     */
    private void write16(final OutputStream os, final int i) throws IOException {
        write8(os, i >> 8 & 0xff);
        write8(os, i & 0xff);
    }

    /**
     * Write 32 bit integer value.
     * 
     * @param os
     * @param i
     * @throws IOException
     */
    private void write32(final OutputStream os, final int i) throws IOException {
        write16(os, i >> 16 & 0xffff);
        write16(os, i & 0xffff);
    }

    /**
     * Write 8 bit integer value.
     * 
     * @param os
     * @param i
     * @throws IOException
     */
    private void write8(final OutputStream os, final int i) throws IOException {
        os.write(i & 0xff);
    }

    /**
     * Write padding up to the next 4 byte (32 bit) boundary.
     * 
     * @param os
     * @param len
     * @throws IOException
     */
    private void writePad(final OutputStream os, final int len) throws IOException {
        int count = len % 4;
        if (count != 0) {
            while (count++ < 4) {
                os.write(0);
            }
        }
    }

    /**
     * Write this DimeRecord to an OutputStream.
     * 
     * @param os
     * @throws IOException
     */
    public void writeTo(final OutputStream os) throws IOException {
        final byte[] buffer = new byte[getChunkSize()];
        final InputStream is = asInputStream();

        int dataLen = is.read(buffer);

        // if data does not fit into buffer, create chunks
        setChunked((dataLen == getChunkSize()));

        int b, b0, b1, b2, b3;

        // version ,start, end and chunked flags
        b0 = VERSION << 3 & 0xf8;
        b1 = isFirst() ? 4 : 0;
        if (isChunked()) {
            // there is at least one other chunk after this one
            b2 = 0;
        } else {
            b2 = isLast() ? 2 : 0;
        }
        b3 = isChunked() ? 1 : 0;
        b = b0 | b1 | b2 | b3;
        write8(os, b);

        // type name format
        b = getTypeNameFormat().value() << 4 & 0xf0;
        write8(os, b);

        // options length
        if (getOptions() != null) {
            write16(os, getOptions().length);
        } else {
            write16(os, 0);
        }

        // id length
        if (getContentId() != null) {
            write16(os, getContentId().getBytes().length);
        } else {
            write16(os, 0);
        }

        // type length
        if (getContentType() != null) {
            write16(os, getContentType().getBytes().length);
        } else {
            write16(os, 0);
        }

        // payload data length
        write32(os, dataLen);

        // options data
        if (getOptions() != null && getOptions().length > 0) {
            os.write(getOptions());
            writePad(os, getOptions().length);
        }

        // id data
        if (getContentId() != null && getContentId().length() > 0) {
            os.write(getContentId().getBytes());
            writePad(os, getContentId().getBytes().length);
        }

        // type data
        if (getContentType() != null && getContentType().length() > 0) {
            os.write(getContentType().getBytes());
            writePad(os, getContentType().getBytes().length);
        }

        // payload data
        os.write(buffer, 0, dataLen);
        writePad(os, dataLen);

        // write remaining chunks - if any
        while (isChunked()) {
            // read next chunk
            dataLen = is.read(buffer);

            // if data still does not fit into buffer, create another chunk
            setChunked((dataLen == getChunkSize()));

            // version ,start, end and chunked flags
            b0 = VERSION << 3 & 0xf8;
            b1 = 0; // a chunk can't be first
            if (isChunked()) {
                // there is at least one other chunk after this one
                b2 = 0;
            } else {
                b2 = isLast() ? 2 : 0;
            }
            b3 = isChunked() ? 1 : 0;
            b = b0 | b1 | b2 | b3;
            write8(os, b);

            // type name format - chunks always have UNCHANGED (i.e. 0) value
            b = TypeNameFormat.UNCHANGED.value() << 4 & 0xf0;
            write8(os, b);

            // options length - chunks have no options
            write16(os, 0);

            // id length - chunks have no id
            write16(os, 0);

            // type length - chunks have no type
            write16(os, 0);

            // payload data length
            write32(os, dataLen);

            // payload data
            os.write(buffer, 0, dataLen);
            writePad(os, dataLen);
        }
    }

    /**
     * Not implemented.
     * 
     * @throws UnsupportedOperationException
     * @see Attachment#writeTo(SOAPMessage)
     */
    public void writeTo(final SOAPMessage saaj) throws SOAPException {
        throw new UnsupportedOperationException();
    }
}
