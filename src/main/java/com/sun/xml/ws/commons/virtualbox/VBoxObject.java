package com.sun.xml.ws.commons.virtualbox;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class VBoxObject {
    public final VboxPortType port;
    public final String _this;

    protected VBoxObject(String _this, VboxPortType port) {
        this._this = _this;
        this.port = port;
    }
}
