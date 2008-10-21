/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.jvnet.jax_ws_commons.transport.grizzly_lwhs.server;

import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpPrincipal;

import java.net.URI;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * A implementation of {@link HttpExchange} using grizzly.
 *
 * @author Jitendra Kotamraju
 */
class LWHSExchange extends HttpExchange {
    private GrizzlyRequest request;
    private GrizzlyResponse response;
    private Headers requestHeaders;
    private Headers responseHeaders;

    LWHSExchange(GrizzlyRequest request, GrizzlyResponse response) {
        this.request = request;
        this.response = response;
        this.requestHeaders = new LWHSRequestHeaders(request);
        this.responseHeaders = new LWHSResponseHeaders(response);
    }

    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    public URI getRequestURI() {
        try {
            return new URI(request.getRequestURI()+"?"+request.getQueryString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRequestMethod() {
        return request.getMethod();
    }

    public HttpContext getHttpContext() {
        return null;
    }

    public void close() {

    }

    public InputStream getRequestBody() {
        try {
            return request.getInputStream();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public OutputStream getResponseBody() {
        try {
            return response.getOutputStream();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        response.setStatus(rCode);
        if (responseLength > 0) {
            response.setContentLengthLong(responseLength);
        }
    }

    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    public int getResponseCode() {
        return 0;
    }

    public InetSocketAddress getLocalAddress() {
        return new InetSocketAddress(request.getLocalAddr(),request.getLocalPort());
    }

    public String getProtocol() {
        return request.getProtocol();
    }

    public Object getAttribute(String name) {
        return null;
    }

    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void setStreams(InputStream i, OutputStream o) {
        throw new UnsupportedOperationException();
    }

    public HttpPrincipal getPrincipal() {
        return null;
    }
}
