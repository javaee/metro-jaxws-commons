package org.jvnet.jax_ws_commons.json.schema;

import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPart;
import com.sun.xml.ws.api.model.wsdl.WSDLPartDescriptor;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchemaSet;

import javax.jws.soap.SOAPBinding.Style;
import java.util.Map;

/**
 * Represents the JSON type signature of an operation.
 * 
 * @author Kohsuke Kawaguchi
 */
public class JsonOperation {

    public final JsonType input,output;

    public JsonOperation(WSDLBoundOperation bo, XSSchemaSet schemas, JsonTypeBuilder builder, Style style) {
        input = build(schemas, bo.getInParts(), builder, style);
        // if the return type has only one property we also unwrap that.
        // see SchemaInfo#createXMLStreamWriter
        output = build(schemas, bo.getOutParts(), builder, style).unwrap();
    }

    /**
     * Infer the JavaScript type from the given parts set.
     *
     */
    private JsonType build(XSSchemaSet schemas, Map<String,WSDLPart> parts, JsonTypeBuilder builder, Style style) {
        CompositeJsonType wrapper = new CompositeJsonType();
        for(Map.Entry<String,WSDLPart> in : parts.entrySet() ) {
            if(!in.getValue().getBinding().isBody())
                continue;   // JSON binding has no header support for now.
            WSDLPartDescriptor d = in.getValue().getDescriptor();

            switch (d.type()) {
            case ELEMENT:
                XSElementDecl decl = schemas.getElementDecl(d.name().getNamespaceURI(), d.name().getLocalPart());
                wrapper.properties.put(in.getKey(),builder.create(decl.getType()));
                break;
            case TYPE:
                wrapper.properties.put(in.getKey(),builder.create(
                    schemas.getType(d.name().getNamespaceURI(), d.name().getLocalPart())));
                break;
            }
        }

        if(style==Style.DOCUMENT)
            // peel off the outermost part that doesn't actually have a representation on the wire.
            return wrapper.unwrap();
        else
            return wrapper;
    }
}
