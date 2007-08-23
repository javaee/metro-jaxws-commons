package com.sun.xml.ws.transport.http.servlet;

import java.util.List;

/**
 * Set of {@link SpringBinding}.
 *
 * @author Kohsuke Kawaguchi
 * @deprecated
 *      Left only for compatibility.
 * @org.apache.xbean.XBean element="bindings"
 */
public class SpringBindingList {
    private List<SpringBinding> bindings;

    public List<SpringBinding> getBindings() {
        return bindings;
    }

    /**
     * Individual bindings. 
     *
     * @org.apache.xbean.Property nestedType="com.sun.xml.ws.transport.http.servlet.SpringBinding" 
     */
    public void setBindings(List<SpringBinding> bindings) {
        this.bindings = bindings;
    }
}
