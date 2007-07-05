/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: WSServletContextListenerWrapper.java
 */

package org.jvnet.jax_ws_commons.beans_generator.servlet.jaxws;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jvnet.jax_ws_commons.beans_generator.ContextBuilder;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.impl.asm.ASMAmbassadorGenerator;
import org.jvnet.jax_ws_commons.beans_generator.conf.IDeploymentConfigurationReader;


import com.sun.xml.ws.transport.http.servlet.WSServletContextListener;

/**
 * Entry point for JAXWS infrastructure.
 * 
 * Created: 13.06.2007
 * 
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class WSServletContextListenerWrapper implements
	ServletContextAttributeListener, ServletContextListener {

    private WSServletContextListener filling = null;

    private Logger log = null;

    public WSServletContextListenerWrapper() {
	super();
	log = Logger.getLogger(this.getClass().getCanonicalName());
    }

    /**
         * Delegate a method call to real executor (non-Javadoc)
         * 
         * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
         */
    public void contextDestroyed(ServletContextEvent arg0) {
	if (null != this.filling)
	    filling.contextDestroyed(arg0);
    }

    /**
         * Delegate a method call to real executor
         */
    public void attributeAdded(ServletContextAttributeEvent arg0) {
	if (null != this.filling)
	    this.filling.attributeAdded(arg0);
    }

    /**
         * Delegate a method call to real executor
         */
    public void attributeRemoved(ServletContextAttributeEvent arg0) {
	if (null != this.filling)
	    this.filling.attributeRemoved(arg0);
    }

    /**
         * Delegate a method call to real executor
         */
    public void attributeReplaced(ServletContextAttributeEvent arg0) {
	if (null != this.filling)
	    this.filling.attributeReplaced(arg0);
    }

    /*
         * (non-Javadoc)
         * 
         * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
         */
    public void contextInitialized(ServletContextEvent context) {

	ASMAmbassadorGenerator generator = null;
	ClassLoader originalCL = null;
	originalCL = Thread.currentThread().getContextClassLoader();

	try {
	    IDeploymentConfigurationReader scr = new JaxwsServletDeploymentConfigurationReader();
	    JaxwsServletDeploymentConfigurationReader
		    .readConfigurationFromContext(context.getServletContext());
	    generator = new ASMAmbassadorGenerator();
	    ContextBuilder cb = new ContextBuilder(scr, generator);
	    cb.build();

	    ClassLoader newCL = generator.getAmbassadorClassLoader(originalCL);

	    // set our new class loader
	    Thread.currentThread().setContextClassLoader(newCL);

	    log.info("Initializing WSServletContextListener...");
	    filling = new WSServletContextListener();
	    filling.contextInitialized(context);
	} catch (Exception e) {
	    log
		    .log(
			    Level.SEVERE,
			    "Exception while initializing 'real' WSServletContextListener.",
			    e);
	} finally {
	    // replace to original class loader
	    Thread.currentThread().setContextClassLoader(originalCL);

	    if (generator != null) {
		log
			.info("Storing our 'Ambassador' generator as context attribute...");
		// let's make free original class loader :)
		context
			.getServletContext()
			.setAttribute(
				RequestClassLoaderSubstituter.AMBASSADOR_CLASS_GENERATOR,
				generator);
	    }
	}
    }
}
