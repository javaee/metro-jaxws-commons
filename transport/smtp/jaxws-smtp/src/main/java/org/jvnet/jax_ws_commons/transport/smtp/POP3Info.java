package org.jvnet.jax_ws_commons.transport.smtp;

/**
 * @org.apache.xbean.XBean element="pop3" root-element="true"
 */
public class POP3Info {
    private String scheme = "pop3";
    private String host;
    private String uid;
    private String password;
    private int interval = 5000;

    public POP3Info() {
    }

    public POP3Info(String host, String uid, String password) {
        setHost(host);
        setUid(uid);
        setPassword(password);
    }

    public int getInterval() {
        return interval;
    }

    /**
     * @org.xbean.Property
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getUid() {
        return uid;
    }

    /**
     * @org.xbean.Property required="true"
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPassword() {
        return password;
    }

    /**
     * @org.xbean.Property required="true"
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getScheme() {
        return scheme;
    }

    /**
     * @org.xbean.Property
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    /**
     * @org.xbean.Property required="true"
     */
    public void setHost(String host) {
        this.host = host;
    }
}
