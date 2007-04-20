package org.jvnet.jax_ws_commons.json.schema;

import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPart;
import com.sun.xml.ws.api.model.wsdl.WSDLPartDescriptor;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchemaSet;
import org.jvnet.jax_ws_commons.json.SchemaConvention;

import javax.jws.WebParam.Mode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class JsonOperation {

    private Map<String,JsonType> inParams = new HashMap<String,JsonType>();
    private Map<String,JsonType> outParams = new HashMap<String,JsonType>();

    public JsonOperation(WSDLBoundOperation bo, XSSchemaSet schemas, SchemaConvention convention) {
        build(schemas, bo.getInParts(), Mode.IN, inParams, convention);
        build(schemas, bo.getOutParts(), Mode.OUT, outParams, convention);
    }

    private void build(XSSchemaSet schemas, Map<String, WSDLPart> parts, Mode mode, Map<String, JsonType> result, SchemaConvention convention) {
        for(Map.Entry<String,WSDLPart> in : parts.entrySet() ) {
            if(!in.getValue().getBinding().isBody())
                continue;   // we only do body
            WSDLPartDescriptor d = in.getValue().getDescriptor();

            switch (d.type()) {
            case ELEMENT:
                XSElementDecl decl = schemas.getElementDecl(d.name().getNamespaceURI(), d.name().getLocalPart());
                result.put(in.getKey(),JsonType.create(convention,decl.getType()));
                break;
            case TYPE:
                result.put(in.getKey(),JsonType.create(convention,
                    schemas.getType(d.name().getNamespaceURI(), d.name().getLocalPart())));
                break;
            }
        }
    }
}
