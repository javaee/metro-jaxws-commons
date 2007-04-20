package org.jvnet.jax_ws_commons.json.schema;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSParticle;
import org.jvnet.jax_ws_commons.json.SchemaConvention;
import org.jvnet.jax_ws_commons.json.SchemaWalker;

import javax.xml.namespace.QName;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map type.
 * @author Kohsuke Kawaguchi
 */
public class CompositeJsonType extends JsonType {

    private final Map<String,JsonType> properties = new LinkedHashMap<String,JsonType>();

    public CompositeJsonType(final SchemaConvention convention, XSComplexType ct) {
        ct.visit(new SchemaWalker() {
            boolean repeated = false;

            public void particle(XSParticle particle) {
                boolean r = repeated;
                repeated |= particle.isRepeated();
                super.particle(particle);
                repeated = r;
            }

            public void elementDecl(XSElementDecl decl) {
                String j = convention.x2j.get(new QName(decl.getTargetNamespace(), decl.getName()));
                if(properties.containsKey(j))
                    // this element shows up more than once.
                    properties.put(j, properties.get(j).makeArray());
                else {
                    JsonType t = JsonType.create(convention, decl.getType());
                    if(repeated)    t=t.makeArray();
                    properties.put(j,t);
                }
            }
        });
    }
}
