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
package com.sun.xml.ws.transport.local;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.servlet.SpringBinding;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;

import java.io.IOException;
import java.util.List;

/**
 * Set of {@link SpringBinding}.
 *
 * @author Kohsuke Kawaguchi
 * @org.apache.xbean.XBean element="bindings" rootElement="true"
 */
public class LocalBinding implements BeanNameAware, FactoryBean {
    private List<WSEndpoint> endpoints;
    private String name;
    private InVmServer server;

    public List<WSEndpoint> getEndpoints() {
        return endpoints;
    }

    /**
     * Individual endpoints.
     *
     * @org.apache.xbean.Property nestedType="com.sun.xml.ws.api.server.WSEndpoint"
     */
    public void setEndpoints(List<WSEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * Bean name is used as the ID of the in-vm server,
     * which becomes the URI to access this in-vm server endpoint
     * (<tt>in-vm://<i>ID</i>/</tt>)
     */
    public void setBeanName(String name) {
        this.name = name;
    }

    /**
     * Obtains the fully-configured {@link InVmServer}.
     */
    public InVmServer getObject() throws IOException {
        if(server==null)
            server = new InVmServer(name,endpoints);
        return server;
    }

    public Class getObjectType() {
        return InVmServer.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
