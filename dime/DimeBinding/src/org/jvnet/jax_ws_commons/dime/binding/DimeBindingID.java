package org.jvnet.jax_ws_commons.dime.binding;

import javax.xml.ws.BindingType;

import org.jvnet.jax_ws_commons.dime.codec.DimeCodec;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;


/**
 * SOAP 1.1 over HTTP binding with DIME encoding for binary attachments.
 * 
 * @author Oliver Treichel
 */
public class DimeBindingID extends BindingID {
    /** Use this value in a {@link BindingType} annotation. */
    // TODO: is there an official value?
    public static final String DIME_BINDING = "http://schemas.xmlsoap.org/ws/2002/04/dime/";

    /** Singleton instance. */
    private static final DimeBindingID SINGLETON_INSTANCE = new DimeBindingID();

    /**
     * Return the singleton instance.
     * 
     * @return The singleton instance.
     */
    public static DimeBindingID getInstance() {
        return SINGLETON_INSTANCE;
    }

    /**
     * for private use only.
     */
    private DimeBindingID() {
        // disabled
    }

    /**
     * We do have a {@link WSDLGeneratorExtension} for this binding. Therefore, this method returns <code>true</code>.
     * 
     * @see BindingID#canGenerateWSDL()
     */
    @Override
    public boolean canGenerateWSDL() {
        return true;
    }

    /**
     * This binding uses it's own codec.
     * 
     * @return An instance of {@link DimeCodec}.
     * @see BindingID#createEncoder(WSBinding)
     */
    @Override
    public Codec createEncoder(final WSBinding binding) {
        return new DimeCodec();
    }

    /**
     * DIME only supports SOAP 1.1. Therefore, this method returns {@link SOAPVersion#SOAP_11}.
     * 
     * @return {@link SOAPVersion#SOAP_11}
     * @see BindingID#getSOAPVersion()
     */
    @Override
    public SOAPVersion getSOAPVersion() {
        return SOAPVersion.SOAP_11;
    }

    /**
     * String representation of this binding.
     */
    @Override
    public String toString() {
        return DIME_BINDING;
    }
}
