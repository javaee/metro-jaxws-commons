package org.jvnet.jax_ws_commons.json.schema;

/**
 * @author Kohsuke Kawaguchi
 */
public class JsonType {
    /**
     * Number primitive type.
     */
    public static final JsonType NUMBER = new JsonType();
    /**
     * Boolean primitive type.
     */
    public static final JsonType BOOLEAN = new JsonType();
    /**
     * String primitive type.
     */
    public static final JsonType STRING = new JsonType();

    public final JsonType makeArray() {
        return new ArrayJsonType(this);
    }

    /**
     * If this object type is the composite type that only has one property, returns its type.
     */
    public JsonType unwrap() {
        return this;
    }
}
