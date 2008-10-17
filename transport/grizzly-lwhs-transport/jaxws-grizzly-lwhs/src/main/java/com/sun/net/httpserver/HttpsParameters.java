/*
 * @(#)HttpsParameters.java	1.4 06/04/28
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.net.httpserver;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Represents the set of parameters for each https
 * connection negotiated with clients. One of these
 * is created and passed to
 * {@link HttpsConfigurator#configure(HttpsParameters)}
 * for every incoming https connection,
 * in order to determine the parameters to use.
 * <p>
 * The underlying SSL parameters may be established either
 * via the set/get methods of this class, or else via 
 * a {@link javax.net.ssl.SSLParameters} object. SSLParameters 
 * is the preferred method, because in the future,
 * additional configuration capabilities may be added to that class, and
 * it is easier to determine the set of supported parameters and their
 * default values with SSLParameters. Also, if an SSLParameters object is 
 * provided via 
 * {@link #setSSLParameters(SSLParameters)} then those parameter settings
 * are used, and any settings made in this object are ignored.
 * @since 1.6
 */
public abstract class HttpsParameters {

    private String[] cipherSuites;
    private String[] protocols;
    private boolean wantClientAuth;
    private boolean needClientAuth;

    protected HttpsParameters() {}

    /**
     * Returns the HttpsConfigurator for this HttpsParameters.
     */
    public abstract HttpsConfigurator getHttpsConfigurator();

    /**
     * Returns the address of the remote client initiating the
     * connection.
     */
    public abstract InetSocketAddress getClientAddress();


    /**
     * Returns a copy of the array of ciphersuites or null if none
     * have been set.
     *
     * @return a copy of the array of ciphersuites or null if none
     * have been set.
     */
    public String[] getCipherSuites() {
        return cipherSuites;
    }

    /**
     * Sets the array of ciphersuites.
     *
     * @param cipherSuites the array of ciphersuites (or null)
     */
    public void setCipherSuites(String[] cipherSuites) { 
	this.cipherSuites = cipherSuites;
    }

    /**
     * Returns a copy of the array of protocols or null if none
     * have been set.
     *
     * @return a copy of the array of protocols or null if none
     * have been set.
     */
    public String[] getProtocols() {
        return protocols;
    }

    /**
     * Sets the array of protocols.
     *
     * @param protocols the array of protocols (or null)
     */
    public void setProtocols(String[] protocols) { 
	this.protocols = protocols;
    }

    /**
     * Returns whether client authentication should be requested.
     *
     * @return whether client authentication should be requested.
     */
    public boolean getWantClientAuth() {
        return wantClientAuth;
    }

    /**
     * Sets whether client authentication should be requested. Calling
     * this method clears the <code>needClientAuth</code> flag.
     *
     * @param wantClientAuth whether client authentication should be requested
     */
    public void setWantClientAuth(boolean wantClientAuth) { 
	this.wantClientAuth = wantClientAuth;
    }

    /**
     * Returns whether client authentication should be required.
     *
     * @return whether client authentication should be required.
     */
    public boolean getNeedClientAuth() {
        return needClientAuth;
    }

    /**
     * Sets whether client authentication should be required. Calling
     * this method clears the <code>wantClientAuth</code> flag.
     *
     * @param needClientAuth whether client authentication should be required
     */
    public void setNeedClientAuth(boolean needClientAuth) { 
	this.needClientAuth = needClientAuth;
    }
}
