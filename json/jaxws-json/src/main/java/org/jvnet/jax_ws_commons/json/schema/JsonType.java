package org.jvnet.jax_ws_commons.json.schema;

import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import org.jvnet.jax_ws_commons.json.SchemaConvention;

import javax.xml.XMLConstants;

/**
 * @author Kohsuke Kawaguchi
 */
public class JsonType {
    /**
     * Number primitive type.
     */
    private static final JsonType NUMBER = new JsonType();
    /**
     * Boolean primitive type.
     */
    private static final JsonType BOOLEAN = new JsonType();
    /**
     * String primitive type.
     */
    private static final JsonType STRING = new JsonType();

    public static JsonType create(SchemaConvention convention, XSType type) {
        if(type.isComplexType())
            return new CompositeJsonType(convention,type.asComplexType());
        else {
            XSSimpleType st = type.asSimpleType();
            if(st.getTargetNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                // built-in
                if(st.getName().equals("decimal")
                || st.getName().equals("float")
                || st.getName().equals("double")) {
                    return JsonType.NUMBER;
                }
                if(st.getName().equals("boolean"))
                    return JsonType.BOOLEAN;
                if(st.getName().equals("anySimpleType"))
                    return JsonType.STRING;
            }
            return create(convention,type.getBaseType());
        }
    }

    public final JsonType makeArray() {
        return new ArrayJsonType(this);
    }
}
