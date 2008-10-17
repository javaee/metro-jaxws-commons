/*
 * %W% %E%
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.net.httpserver;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.security.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.*;


/**
 * This class is used to configure the https parameters for each incoming
 * https connection on a HttpsServer. Applications need to override
 * the {@link #configure(HttpsParameters)} method in order to change
 * the default configuration.
 * <p>
 * The following <a name="example">example</a> shows how this may be done:
 * <p>
 * <pre><blockquote>
 * SSLContext sslContext = SSLContext.getInstance (....);
 * HttpsServer server = HttpsServer.create();
 * 
 * server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
 *     public void configure (HttpsParameters params) {
 * 
 *         // get the remote address if needed
 *         InetSocketAddress remote = params.getClientAddress();
 * 
 *         SSLContext c = getSSLContext();
 * 
 *         // get the default parameters
 *         SSLParameters sslparams = c.getDefaultSSLParameters();
 *         if (remote.equals (...) ) {
 *             // modify the default set for client x
 *         }
 * 
 *         params.setSSLParameters(sslparams);
 *     }
 * }); 
 * </blockquote></pre>
 * @since 1.6
 */
public class HttpsConfigurator {

    private SSLContext context;

    /**
     * Creates an Https configuration, with the given SSLContext.
     * @param context the SSLContext to use for this configurator
     * @throws NullPointerException if no SSLContext supplied
     */
    public HttpsConfigurator (SSLContext context) {
	if (context == null) {
	    throw new NullPointerException ("null SSLContext");
	}
	this.context = context;
    }

    /**
     * Returns the SSLContext for this HttpsConfigurator.
     * @return the SSLContext
     */
    public SSLContext getSSLContext() {
	return context;
    }

}
