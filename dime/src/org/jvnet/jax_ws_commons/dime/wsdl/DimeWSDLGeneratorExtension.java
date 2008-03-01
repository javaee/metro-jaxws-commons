package org.jvnet.jax_ws_commons.dime.wsdl;

import org.jvnet.jax_ws_commons.dime.annotation.DimeInput;
import org.jvnet.jax_ws_commons.dime.annotation.DimeOutput;
import org.jvnet.jax_ws_commons.dime.binding.DimeBindingID;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.wsdl.writer.WSDLGenExtnContext;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;


/**
 * WSDLGeneratorExtension that adds DIME information to the generated WSDL. Adds the DIME name space declaration if the
 * web service uses a {@link DimeBindingID}. Adds a &lt;dime:message&gt; tag to each binding operation that either has
 * a {@link DimeInput} or a {@link DimeOutput} method annotation.
 * 
 * @author Oliver Treichel
 */
public class DimeWSDLGeneratorExtension extends WSDLGeneratorExtension {
    /** DIME closed layout attribute value */
    private static final String DIME_CLOSED_LAYOUT = "http://schemas.xmlsoap.org/ws/2002/04/dime/closed-layout";

    /** DIME layout attribute of the message element */
    private static final String DIME_LAYOUT = "layout";

    /** DIME message element */
    private static final String DIME_MESSAGE = "message";

    /** DIME name space prefix */
    private static final String DIME_NS_PREFIX = "dime";

    /** DIME name space URI */
    private static final String DIME_NS_URI = "http://schemas.xmlsoap.org/ws/2002/04/dime/wsdl/";

    /** WSDL name space prefix */
    private static final String WSDL_NS_PREFIX = "wsdl";

    /** WSDL name space URI */
    private static final String WSDL_NS_URI = "http://schemas.xmlsoap.org/wsdl/";

    /** WSDL required attribute */
    private static final String WSDL_REQUIRED = "required";

    /** Does the endpoint use {@link DimeBindingID}? */
    private boolean isDimeBinding;

    /**
     * Add a &lt;dime:message&gt; tag to the binding operation if the endpoint method has a {@link DimeInput}
     * annotation.
     * 
     * @see WSDLGeneratorExtension#addBindingOperationInputExtension(TypedXmlWriter, JavaMethod)
     */
    @Override
    public void addBindingOperationInputExtension(final TypedXmlWriter input, final JavaMethod method) {
        // add dime:layout element to the input message if the endpoint method
        // has a DimeInput annotation
        if (isDimeBinding && method.getSEIMethod().getAnnotation(DimeInput.class) != null) {
            final TypedXmlWriter dimeMessage = input._element(DIME_NS_URI, DIME_MESSAGE, TypedXmlWriter.class);
            dimeMessage._attribute(DIME_LAYOUT, DIME_CLOSED_LAYOUT);
            dimeMessage._attribute(WSDL_NS_URI, WSDL_REQUIRED, Boolean.TRUE.toString());
        }
    }

    /**
     * Add a &lt;dime:message&gt; tag to the binding operation if the endpoint method has a {@link DimeOutput}
     * annotation.
     * 
     * @see WSDLGeneratorExtension#addBindingOperationOutputExtension(TypedXmlWriter, JavaMethod)
     */
    @Override
    public void addBindingOperationOutputExtension(final TypedXmlWriter output, final JavaMethod method) {
        // add dime:layout element to the output message if the endpoint method
        // has a DimeOutput annotation
        if (isDimeBinding && method.getSEIMethod().getAnnotation(DimeOutput.class) != null) {
            final TypedXmlWriter dimeMessage = output._element(DIME_NS_URI, DIME_MESSAGE, TypedXmlWriter.class);
            dimeMessage._attribute(DIME_LAYOUT, DIME_CLOSED_LAYOUT);
            dimeMessage._attribute(WSDL_NS_URI, WSDL_REQUIRED, Boolean.TRUE.toString());
        }
    }

    /**
     * Declare extra XML name spaces.
     * 
     * @see WSDLGeneratorExtension#addDefinitionsExtension(TypedXmlWriter)
     */
    @Override
    public void addDefinitionsExtension(final TypedXmlWriter definitions) {
        if (isDimeBinding) {
            // add the dime name space declaration
            definitions._namespace(DIME_NS_URI, DIME_NS_PREFIX);
            definitions._namespace(WSDL_NS_URI, WSDL_NS_PREFIX);
        }
    }

    /**
     * Checks is the endpoint uses a {@link DimeBindingID}
     * 
     * @see WSDLGeneratorExtension#start(WSDLGenExtnContext)
     */
    @Override
    public void start(final WSDLGenExtnContext ctx) {
        isDimeBinding = ctx.getBinding().getBindingId() == DimeBindingID.getInstance();
    }
}
