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
package org.jvnet.jax_ws_commons.transport.grizzly_httpspi;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import javax.xml.ws.spi.http.HttpContext;
import javax.xml.ws.spi.http.HttpExchange;
import java.net.InetSocketAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A implementation of {@link HttpExchange} using grizzly.
 *
 * @author Jitendra Kotamraju
 */
class GrizzlyExchange extends HttpExchange {
    private final Request request;
    private final Response response;
    private Map<String, List<String>> requestHeaders;
    private Map<String, List<String>> responseHeaders;
    private final GrizzlyHttpContext context;

    GrizzlyExchange(GrizzlyHttpContext context, Request request, Response response) {
        this.context = context;
        this.request = request;
        this.response = response;
    }

    @Override
    public Map<String, List<String>> getRequestHeaders() {
        if (requestHeaders == null) {
            requestHeaders = new RequestHeaders(request);
        }
        return requestHeaders;
    }

    @Override
    public String getRequestHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        if (responseHeaders == null) {
            responseHeaders = new ResponseHeaders(response);
        }
        return responseHeaders;
    }

    @Override
    public void addResponseHeader(String name, String value) {
        response.addHeader(name, value);
    }

    @Override
    public String getRequestURI() {
        return request.getRequestURI()+"?"+request.getQueryString();
    }

    @Override
    public String getContextPath() {
        return context.getContextPath();
    }

    @Override
    public String getRequestMethod() {
        return request.getMethod().getMethodString();
    }

    @Override
    public HttpContext getHttpContext() {
        return context;
    }

    @Override
    public void close() throws IOException {
        response.finish();
    }

    @Override
    public InputStream getRequestBody() throws IOException {
        return request.getInputStream(true);
    }

    @Override
    public OutputStream getResponseBody() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public void setStatus(int i) {
        response.setStatus(i);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return new InetSocketAddress(request.getLocalAddr(),request.getLocalPort());
    }

    @Override
    public String getProtocol() {
        return request.getProtocol().getProtocolString();
    }

    @Override
    public String getScheme() {
        return request.getScheme();
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Set<String> getAttributeNames() {
        return null;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

}
