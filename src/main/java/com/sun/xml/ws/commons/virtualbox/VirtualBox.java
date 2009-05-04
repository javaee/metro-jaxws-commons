/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package com.sun.xml.ws.commons.virtualbox;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.net.URL;

/**
 * Entry point to VirtualBox web service API.
 *
 * @author Kohsuke Kawaguchi
 */
public class VirtualBox {
    /**
     * Connects to VirtualBox at the specified URL.
     *
     * <p>
     * A particularly noteworthy method of {@link IVirtualBox}
     * is {@link IVirtualBox#getSessionObject()}, which lets
     * you create a new {@link ISession}.
     */
    public static IVirtualBox connect(URL url) {
        return connect(url.toExternalForm());
    }

    /**
     * Type unsafe version of the {@link #connect(URL)}
     */
    public static IVirtualBox connect(String url) {
        // working around https://jax-ws.dev.java.net/issues/show_bug.cgi?id=554
        // this is also necessary when context classloader doesn't have the JAX-WS API
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(IVirtualBox.class.getClassLoader());
        try {
            URL wsdl = VirtualBox.class.getClassLoader().getResource("vboxwebService.wsdl");
            if(wsdl==null)
                throw new LinkageError("vboxwebService.wsdl not found, but it should have been in the jar");
            VboxService svc = new VboxService(wsdl,new QName("http://www.virtualbox.org/Service", "vboxService"));
            VboxPortType port = svc.getVboxServicePort();
            ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
            String vbox = port.iWebsessionManagerLogon("foo","bar");
            if("".equals(vbox))
                throw new WebServiceException("Failed to login. Maybe you need to do 'VBoxManage setproperty websrvauthlibrary null'?"); 
            return new IVirtualBox(vbox,port);
        } catch (InvalidObjectFaultMsg e) {
            throw new WebServiceException(e);
        } catch (RuntimeFaultMsg e) {
            throw new WebServiceException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
}
