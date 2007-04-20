package org.jvnet.jax_ws_commons.json.schema;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JavaScript object.
 *
 * @author Kohsuke Kawaguchi
 */
public class CompositeJsonType extends JsonType {

    public final Map<String,JsonType> properties = new LinkedHashMap<String,JsonType>();

    public CompositeJsonType() {}

    @Override
    public JsonType unwrap() {
        if(properties.size()!=1)
            return this;
        else
            return properties.values().iterator().next();
    }
}
