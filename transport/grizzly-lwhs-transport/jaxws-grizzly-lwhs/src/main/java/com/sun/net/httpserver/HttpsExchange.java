/*
 * @(#)HttpsExchange.java	1.3 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.net.httpserver;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import javax.net.ssl.*;
import java.util.*;

/**
 * This class encapsulates a HTTPS request received and a 
 * response to be generated in one exchange and defines
 * the extensions to HttpExchange that are specific to the HTTPS protocol.
 * @since 1.6
 */

public abstract class HttpsExchange extends HttpExchange {

    protected HttpsExchange () {
    }

    /**
     * Get the SSLSession for this exchange.
     * @return the SSLSession
     */
    public abstract SSLSession getSSLSession ();
}
