package org.jvnet.jax_ws_commons.json.schema;

import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPart;
import com.sun.xml.ws.api.model.wsdl.WSDLPartDescriptor;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchemaSet;
import org.jvnet.jax_ws_commons.json.SchemaConvention;

import javax.jws.soap.SOAPBinding.Style;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class JsonOperation {

    private JsonType input;
    private JsonType output;

    public JsonOperation(WSDLBoundOperation bo, XSSchemaSet schemas, SchemaConvention convention, Style style) {
        input = build(schemas, bo.getInParts(), convention, style);
        output = build(schemas, bo.getOutParts(), convention, style);
    }

    private JsonType build(XSSchemaSet schemas, Map<String, WSDLPart> parts, SchemaConvention convention, Style style) {
        CompositeJsonType wrapper = new CompositeJsonType();
        for(Map.Entry<String,WSDLPart> in : parts.entrySet() ) {
            if(!in.getValue().getBinding().isBody())
                continue;   // we only do body
            WSDLPartDescriptor d = in.getValue().getDescriptor();

            switch (d.type()) {
            case ELEMENT:
                XSElementDecl decl = schemas.getElementDecl(d.name().getNamespaceURI(), d.name().getLocalPart());
                wrapper.properties.put(in.getKey(),JsonType.create(convention,decl.getType()));
                break;
            case TYPE:
                wrapper.properties.put(in.getKey(),JsonType.create(convention,
                    schemas.getType(d.name().getNamespaceURI(), d.name().getLocalPart())));
                break;
            }
        }

        if(style==Style.DOCUMENT)
            // peel off the outermost part that doesn't actually have a representation on the wire.
            return wrapper.properties.values().iterator().next();
        else
            return wrapper;
    }
}
