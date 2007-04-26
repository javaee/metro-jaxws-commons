package org.jvnet.jax_ws_commons.transport.smtp;

/**
 * @org.apache.xbean.XBean element="sender" root-element="true"
 */
public class SenderInfo {
    private String host;
    private String port;
    private String from;

    public SenderInfo() {
    }

    public SenderInfo(String host, String port, String from) {
        this.host = host;
        this.port = port;
        this.from = from;
    }

    /**
     * @org.xbean.Property
     */
    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    /**
     * @org.xbean.Property required="true"
     */
    public void setHost(String host) {
        this.host= host;
    }

    public String getHost() {
        return host;
    }
    
    /**
     * @org.xbean.Property
     */
    public void setPort(String port) {
        this.port= port;
    }

    public String getPort() {
        return port;
    }

}
