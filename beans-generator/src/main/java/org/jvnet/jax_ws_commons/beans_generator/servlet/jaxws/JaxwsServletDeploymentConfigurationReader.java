/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: JaxwsServletDeploymentConfigurationReader.java
 */

package org.jvnet.jax_ws_commons.beans_generator.servlet.jaxws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jvnet.jax_ws_commons.beans_generator.conf.DeploymentConfigurationReadingException;
import org.jvnet.jax_ws_commons.beans_generator.conf.IDeploymentConfigurationReader;
import org.jvnet.jax_ws_commons.beans_generator.conf.IEndpointData;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Created: 15.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class JaxwsServletDeploymentConfigurationReader implements
	IDeploymentConfigurationReader {

    private DocumentBuilder db = null;

    public JaxwsServletDeploymentConfigurationReader() {
	super();
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	try {
	    db = dbf.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	}
    }

    private static String configuration = "";

    public static void readConfigurationFromContext(ServletContext ctx)
	    throws IOException {
	InputStream is = ctx.getResourceAsStream("WEB-INF/sun-jaxws.xml");
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	int c = 0;
	while ((c = is.read()) != -1)
	    baos.write(c);
	configuration = baos.toString();
    }

    /* (non-Javadoc)
     * @see org.jvnet.jax_ws_commons.beans_generator.conf.IDeploymentConfigurationReader#readConfiguration()
     */
    public IEndpointData readConfiguration()
	    throws DeploymentConfigurationReadingException {

	ByteArrayInputStream bais = new ByteArrayInputStream(configuration.getBytes());
	try {
	    return getEndpointDataFromStream(bais);
	} catch (Exception e) {
	    throw new DeploymentConfigurationReadingException(e);
	}
    }

    class EndpointData implements IEndpointData {
	List<IEndpointData.EndpointConfiguration> epClasses = new ArrayList<IEndpointData.EndpointConfiguration>();

	public boolean add(IEndpointData.EndpointConfiguration o) {
	    return this.epClasses.add(o);
	}

	public Collection<IEndpointData.EndpointConfiguration> getEndpointPairs() {
	    return epClasses;
	}

    }

    protected IEndpointData getEndpointDataFromStream(InputStream is)
	    throws SAXException, IOException {
	EndpointData result = new EndpointData();
	Document doc = db.parse(is);
	NodeList nl = doc.getElementsByTagName("endpoint");
	for (int i = 0; i < nl.getLength(); i++) {
	    Node epTag = nl.item(i);
	    NamedNodeMap nnm = epTag.getAttributes();
	    String invokableClassName = getAttributeValue(nnm, "implementation");
	    String wrappedClassName = getAttributeValue(nnm, "wrappedClass");
	    String invoker = getAttributeValue(nnm, "implementationInvoker");

	    boolean ntw = false;
	    if (wrappedClassName != null && invoker != null)
		ntw = true;
	    IEndpointData.EndpointConfiguration ep = new IEndpointData.EndpointConfiguration(wrappedClassName, invokableClassName, invoker, ntw);
	    result.add(ep);
	}
	return result;
    }

    protected String getAttributeValue(NamedNodeMap nnm, String attrName) {
	Node node = nnm.getNamedItem(attrName);
	return (node != null ? node.getNodeValue() : null);
    }
}
