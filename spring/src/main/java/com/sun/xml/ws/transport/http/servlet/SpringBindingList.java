package com.sun.xml.ws.transport.http.servlet;

import java.util.List;

/**
 * Set of {@link SpringBinding}.
 *
 * @author Kohsuke Kawaguchi
 * @org.apache.xbean.XBean element="bindings" rootElement="true"
 */
public class SpringBindingList {
    private List<SpringBinding> bindings;

    public List<SpringBinding> getBindings() {
        return bindings;
    }

    public void setBindings(List<SpringBinding> bindings) {
        this.bindings = bindings;
    }

    public ServletAdapterList create() {
        ServletAdapterList l = new ServletAdapterList();
        for (SpringBinding binding : bindings)
            binding.create(l);
        return l;
    }
}
