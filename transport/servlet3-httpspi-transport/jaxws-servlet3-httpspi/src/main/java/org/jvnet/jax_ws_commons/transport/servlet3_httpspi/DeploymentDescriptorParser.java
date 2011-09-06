/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses {@code webservices.xml}
 *
 * @author Jitendra Kotamraju
 */
class DeploymentDescriptorParser {
    private static final String EENS = "http://java.sun.com/xml/ns/javaee";


    private static final QName WEBSERVICES =
        new QName(EENS, "webservices");
    private static final String VERSION = "version";
    private static final QName WEBSERVICE_DESCRIPTION =
        new QName(EENS, "webservice-description");
    private static final QName WEBSERVICE_DESCRIPTION_NAME =
        new QName(EENS, "webservice-description-name");
    private static final QName WSDL_FILE =
        new QName(EENS, "wsdl-file");
    private static final QName JAXRPC_MAPPING_FILE =
        new QName(EENS, "jaxrpc-mapping-file");

    private static final QName PORT_COMPONENT =
        new QName(EENS, "port-component");
    private static final QName PORT_COMPONENT_NAME =
        new QName(EENS, "port-component-name");
    private static final QName WSDL_PORT =
        new QName(EENS, "wsdl-port");
    private static final QName SERVICE_ENDPOINT_INTERFACE =
        new QName(EENS, "service-endpoint-interface");

    private static final QName ENABLE_MTOM =
        new QName(EENS, "enable-mtom");
    private static final QName MTOM_THRESHOLD =
        new QName(EENS, "mtom-threshold");
    private static final QName SERVICE_IMPL_BEAN =
        new QName(EENS, "service-impl-bean");
    private static final QName SERVLET_LINK =
        new QName(EENS, "servlet-link");
    private static final QName HANDLER =
        new QName(EENS, "handler");
    private static final QName ADDRESSING =
        new QName(EENS, "addressing");
        private static final QName RESPECT_BINDING =
        new QName(EENS, "respect-binding");

    private static final Logger LOGGER =
        Logger.getLogger(DeploymentDescriptorParser.class.getName());

    private final ClassLoader classLoader;
    private static final XMLInputFactory xif = XMLInputFactory.newInstance();

    DeploymentDescriptorParser() {
        classLoader = Thread.currentThread().getContextClassLoader();
    }

    // Can be used for unit testing
    static List<EndpointInfo> parse(String systemId, InputStream is) {
        XMLStreamReader reader = null;
        try {
            synchronized(xif) {
                reader = xif.createXMLStreamReader(systemId, is);
            }
            nextElementContent(reader);
            return parseEndpoints(reader);
        } catch(IOException|XMLStreamException e) {
            throw new WebServiceException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    // ignore
                }
            }
        }
    }

    private static int nextElementContent(XMLStreamReader reader) throws XMLStreamException {
        do {
            int state = reader.next();
            if (state == START_ELEMENT || state == END_ELEMENT || state == END_DOCUMENT) {
                return state;
            }
        } while(true);
    }

    static List<EndpointInfo> parseEndpoints(XMLStreamReader reader) throws IOException, XMLStreamException {
        if (!reader.getName().equals(WEBSERVICES)) {
            throw new WebServiceException("Unexpected element="+reader.getName()+" at line "+reader.getLocation().getLineNumber());
        }

        String version = reader.getAttributeValue("", VERSION);
        if (!version.equals("1.3")) {
            throw new WebServiceException("webservices.xml's version attribute is other than 1.3, it is "+version);
        }
        nextElementContent(reader);

        List<EndpointInfo> endpointInfoList = new ArrayList<>();

        while (reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
            if (reader.getName().equals(WEBSERVICE_DESCRIPTION)) {
                parseWebServiceDescription(reader, endpointInfoList);
            } else {
                throw new WebServiceException("Unexpected element="+reader.getName()+" at line "+reader.getLocation().getLineNumber());
            }
        }
        return endpointInfoList;
    }

    static void parseWebServiceDescription(XMLStreamReader reader, List<EndpointInfo> endpointInfoList) throws IOException, XMLStreamException {
        nextElementContent(reader);
        String name = parseWebServiceDescriptionName(reader);
        String wsdlFile = null;
        if (reader.getName().equals(WSDL_FILE)) {
            wsdlFile = parseWsdlFile(reader);
        }
        if (reader.getName().equals(JAXRPC_MAPPING_FILE)) {
            String mappingFile = parseJaxRpcMappingFile(reader);
        }
        while(reader.getName().equals(PORT_COMPONENT)) {
            EndpointInfo endpointinfo = parsePortComponent(reader);
            endpointinfo.wsdlFile = wsdlFile;
            endpointInfoList.add(endpointinfo);
        }
        nextElementContent(reader);
    }

    static String parseWebServiceDescriptionName(XMLStreamReader reader) throws IOException, XMLStreamException {
        assert reader.getName().equals(WEBSERVICE_DESCRIPTION_NAME);
        String name = reader.getElementText().trim();
        nextElementContent(reader);
        return name;
    }

    static String parseWsdlFile(XMLStreamReader reader) throws IOException, XMLStreamException {
        String wsdlFile = reader.getElementText().trim();
        nextElementContent(reader);
        return wsdlFile;
    }

    static String parseJaxRpcMappingFile(XMLStreamReader reader) throws IOException, XMLStreamException {
        String mappingFile = reader.getElementText().trim();
        nextElementContent(reader);
        return mappingFile;
    }

    static EndpointInfo parsePortComponent(XMLStreamReader reader) throws IOException, XMLStreamException {
        nextElementContent(reader);
        EndpointInfo endpointInfo = new EndpointInfo();
        parsePortComponentName(reader, endpointInfo);
        if (reader.getName().equals(WSDL_PORT)) {
            parseWsdlPort(reader, endpointInfo);
        }
        if (reader.getName().equals(SERVICE_ENDPOINT_INTERFACE)) {
            parseServiceEndpointInterface(reader, endpointInfo);
        }
        if (reader.getName().equals(ENABLE_MTOM)) {
            parseEnableMtom(reader, endpointInfo);
        }
        if (reader.getName().equals(MTOM_THRESHOLD)) {
            parseMtomThreshold(reader, endpointInfo);
        }
        if (reader.getName().equals(ADDRESSING)) {
            parseAddressing(reader, endpointInfo);
        }
        if (reader.getName().equals(RESPECT_BINDING)) {
            parseRespectBinding(reader, endpointInfo);
        }
        if (reader.getName().equals(SERVICE_IMPL_BEAN)) {
            parseServiceImplBean(reader, endpointInfo);
        }
        if (reader.getName().equals(HANDLER)) {
            parseHandler(reader, endpointInfo);
        }
        nextElementContent(reader);
        return endpointInfo;
    }

    static void parsePortComponentName(XMLStreamReader reader, EndpointInfo endpointInfo) throws IOException, XMLStreamException {
        assert reader.getName().equals(PORT_COMPONENT_NAME);
        String portComponentName = reader.getElementText();
        portComponentName = portComponentName.trim();
        endpointInfo.portComponentName = portComponentName;
        nextElementContent(reader);
    }

    static void parseWsdlPort(XMLStreamReader reader, EndpointInfo endpointInfo) throws IOException, XMLStreamException {
        String wsdlPortStr = reader.getElementText();
        wsdlPortStr = wsdlPortStr.trim();
        int index = wsdlPortStr.indexOf(':');
        QName wsdlPort;
        if (index == -1) {
            wsdlPort = new QName(reader.getNamespaceURI(), wsdlPortStr);
        } else {
            String prefix = wsdlPortStr.substring(0, index);
            String local = wsdlPortStr.substring(index+1);
            wsdlPort = new QName(reader.getNamespaceURI(prefix), local);
        }

        endpointInfo.wsdlPort = wsdlPort;
        nextElementContent(reader);
    }

    static void parseServiceEndpointInterface(XMLStreamReader reader, EndpointInfo endpointInfo) throws IOException, XMLStreamException {
        skipElement(reader);
    }

    static void parseEnableMtom(XMLStreamReader reader, EndpointInfo endpointInfo) throws IOException, XMLStreamException {
        skipElement(reader);
    }

    static void parseMtomThreshold(XMLStreamReader reader, EndpointInfo endpointInfo) throws IOException, XMLStreamException {
        skipElement(reader);
    }

    static void parseServiceImplBean(XMLStreamReader reader, EndpointInfo endpointInfo) throws IOException, XMLStreamException {
        nextElementContent(reader);
        if (reader.getName().equals(SERVLET_LINK)) {
            parseServletLink(reader, endpointInfo);
        }
        nextElementContent(reader);
    }

    static void parseServletLink(XMLStreamReader reader, EndpointInfo endpointInfo) throws IOException, XMLStreamException {
        endpointInfo.servletLink = reader.getElementText().trim();
        nextElementContent(reader);
    }

    static void parseAddressing(XMLStreamReader reader, EndpointInfo endpointInfo) throws IOException, XMLStreamException {
        skipElement(reader);
    }

    static void parseRespectBinding(XMLStreamReader reader, EndpointInfo endpointInfo) throws IOException, XMLStreamException {
        skipElement(reader);
    }

    static void parseHandler(XMLStreamReader reader, EndpointInfo endpointInfo) throws IOException, XMLStreamException {
        skipElement(reader);
    }
    
    /**
     * JSR-109 defines short-form tokens for standard binding Ids. These are
     * used only in DD. So stand alone deployment descirptor should also honor
     * these tokens. This method converts the tokens to API's standard
     * binding ids
     *
     * @param lexical binding attribute value from DD. Always not null
     *
     * @return returns corresponding API's binding ID or the same lexical
     */
    private static String getBindingIdForToken(String lexical) {
        switch (lexical) {
            case "##SOAP11_HTTP":
                return SOAPBinding.SOAP11HTTP_BINDING;
            case "##SOAP11_HTTP_MTOM":
                return SOAPBinding.SOAP11HTTP_MTOM_BINDING;
            case "##SOAP12_HTTP":
                return SOAPBinding.SOAP12HTTP_BINDING;
            case "##SOAP12_HTTP_MTOM":
                return SOAPBinding.SOAP12HTTP_MTOM_BINDING;
            case "##XML_HTTP":
                return HTTPBinding.HTTP_BINDING;
        }
        return lexical;
    }

    protected Class loadClass(String name) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new WebServiceException(e);
        }
    }


    /**
     * Loads the class of the given name.
     *
     * @param xsr
     *      Used to report the source location information if there's any error.
     */
    private Class getImplementorClass(String name, XMLStreamReader xsr) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new WebServiceException("Class at "+xsr.getLocation().getLineNumber()+" is not found",e);
        }
    }

    /**
     * Skip current element, leaving the cursor at START_ELEMENT of
     * next element, or END_ELEMENT of parent, or END_DOCUMENT)
     */
    private static void skipElement(XMLStreamReader reader) throws XMLStreamException {
        assert reader.getEventType() == START_ELEMENT;
        skipTags(reader);
        assert reader.getEventType() == START_ELEMENT || reader.getEventType() == END_ELEMENT || reader.getEventType() == END_DOCUMENT;
    }

    private static void skipTags(XMLStreamReader reader) throws XMLStreamException {
        int tags = 0;
        while (true) {
            int state = reader.next();
            if (state == START_ELEMENT) {
                tags++;
            } else if (state == END_ELEMENT) {
                if (tags == 0) {
                    break;
                }
                tags--;
            }
        }
        nextElementContent(reader);
    }

}
