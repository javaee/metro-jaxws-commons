/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.jvnet.jax_ws_commons.transport.servlet3_httpspi;

import javax.jws.WebService;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.WebServiceProvider;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Jitendra Kotamraju
 */
@HandlesTypes({WebService.class, WebServiceProvider.class})
public class WSServletContainerInitializer implements ServletContainerInitializer {
    private static final Logger LOGGER = Logger.getLogger(WSServletContainerInitializer.class.getName());
    private static final String JAXWS_WSDL_DD_DIR = "/WEB-INF/wsdl/";
    private static final String WEBSERVICES_XML = "/WEB-INF/webservices.xml";

    public void onStartup(Set<Class<?>> wsClassSet, ServletContext context) throws ServletException {
        LOGGER.info("WAR has webservice classes: "+wsClassSet);

        // TODO Check if metadata-complete is true in web.xml

        // Get the WSDL and schema documents
        List<Source> metadata = new ArrayList<>();
        try {
            Set<URL> docs = new HashSet<>();
            collectDocs(docs, new ServletResourceLoader(context), JAXWS_WSDL_DD_DIR);
            for(URL url : docs) {
                Source source = new StreamSource(url.openStream(), url.toExternalForm());
                metadata.add(source);
            }
        } catch (IOException me) {
            throw new ServletException(me);
        }

        // Parse webservices.xml
        List<EndpointInfo> ddEndpointList = parseDD(context);

        // Consolidate endpoints from DD and annotations
        List<EndpointInfo> endpointInfoList = getConsolidatedEndpoints(ddEndpointList, wsClassSet, metadata);
        if (endpointInfoList.isEmpty()) {
            return;             // No web service endpoints
        }

        ServletRegistration.Dynamic reg = context.addServlet("JAX-WS-Servlet", WSServlet.class);

        EndpointAdapterFactory factory = new EndpointAdapterFactory();

        List<EndpointAdapter> adapters = new ArrayList<>();

        for(EndpointInfo endpointInfo : endpointInfoList) {
            EndpointAdapter adapter = factory.createAdapter(endpointInfo);
            adapters.add(adapter);
            reg.addMapping(endpointInfo.urlPattern);
        }

        for(EndpointAdapter adapter : adapters) {
            adapter.publish();
        }

        context.setAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO, adapters);
    }

    private List<EndpointInfo> getConsolidatedEndpoints(
            List<EndpointInfo> ddEndpointList, Set<Class<?>> wsClassSet, List<Source> metadata) {
        List<EndpointInfo> consList = new ArrayList<>();
        for(Class<?> c : wsClassSet) {
            EndpointInfo endpointInfo = new EndpointInfo();
            endpointInfo.implType = c;
            endpointInfo.urlPattern = "/"+c.getName();
            endpointInfo.features = new WebServiceFeature[0];
            endpointInfo.metadata = metadata;
            consList.add(endpointInfo);
        }
        return consList;
    }


    /*
     * Get all the WSDL & schema documents recursively.
     */
    private void collectDocs(Set<URL> docs, ServletResourceLoader loader, String dirPath) throws MalformedURLException {
        Set<String> paths = loader.getResourcePaths(dirPath);
        if (paths != null) {
            for (String path : paths) {
                if (path.endsWith("/")) {
                    if(path.endsWith("/CVS/") || path.endsWith("/.svn/"))
                        continue;
                    collectDocs(docs, loader, path);
                } else {
                    URL res = loader.getResource(path);
                    docs.add(res);
                }
            }
        }
    }


    /*
     * Parses the {@code webservices.xml} file and returns
     * a list of {@link EndpointInfo}s.
     */
    private List<EndpointInfo> parseDD(ServletContext context) {
        URL webservicesXml;
        try {
            webservicesXml = context.getResource(WEBSERVICES_XML);
        } catch (MalformedURLException e) {
            throw new WebServiceException(e);
        }
        if (webservicesXml==null) {
            return Collections.emptyList();
        }

        // TODO validate against schema

        try(InputStream is=webservicesXml.openStream()) {
            return DeploymentDescriptorParser.parse(webservicesXml.toExternalForm(), is);
        } catch(IOException e) {
            throw new WebServiceException(e);
        }
    }
}