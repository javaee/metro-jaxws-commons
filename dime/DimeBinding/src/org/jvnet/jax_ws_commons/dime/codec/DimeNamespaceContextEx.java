package org.jvnet.jax_ws_commons.dime.codec;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import org.jvnet.staxex.NamespaceContextEx;
import org.jvnet.staxex.XMLStreamReaderEx;
import org.jvnet.staxex.XMLStreamWriterEx;

/**
 * {@link XMLStreamReaderEx} and {@link XMLStreamWriterEx} need an implementation of {@link NamespaceContextEx}. This
 * class simply wraps a standard {@link NamespaceContext} and add the missing methods.
 * 
 * @author oli
 */
class DimeNamespaceContextEx implements NamespaceContextEx {
    /** The delegate {@link NamespaceContext}, that does the actual work. */
    private final NamespaceContext delegate;

    /**
     * Constructor for a wrapper around a standard {@link NamespaceContext}.
     * 
     * @param delegate
     *            The delegate, that does the actual work.
     */
    public DimeNamespaceContextEx(final NamespaceContext delegate) {
        this.delegate = delegate;
    }

    /**
     * @see NamespaceContext#getNamespaceURI(String)
     */
    public String getNamespaceURI(final String prefix) {
        return delegate.getNamespaceURI(prefix);
    }

    /**
     * @see NamespaceContext#getPrefix(String)
     */
    public String getPrefix(final String namespaceURI) {
        return delegate.getPrefix(namespaceURI);
    }

    /**
     * @see NamespaceContext#getPrefixes(String)
     */
    @SuppressWarnings("unchecked")
    public Iterator getPrefixes(final String namespaceURI) {
        return delegate.getPrefixes(namespaceURI);
    }

    /**
     * Not implemented.
     * 
     * @throws UnsupportedOperationException
     * @see NamespaceContextEx#iterator()
     */
    public Iterator<Binding> iterator() {
        throw new UnsupportedOperationException();
    }
}