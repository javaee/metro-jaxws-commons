/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: RequestClassLoaderSubstituter.java
 */

package org.jvnet.jax_ws_commons.beans_generator.servlet.jaxws;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.jvnet.jax_ws_commons.beans_generator.ambassador.IAmbassadorGenerator;


/**
 * Created: 13.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class RequestClassLoaderSubstituter implements ServletRequestListener {

    private ClassLoader originalCL = null;

    public static final String AMBASSADOR_CLASS_GENERATOR =
	    "org.jvnet.jax_ws_commons.beans_generator.base-classloader";

    public void requestDestroyed(ServletRequestEvent arg0) {
	Thread.currentThread().setContextClassLoader(originalCL);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestListener#requestInitialized(javax.servlet.ServletRequestEvent)
     */
    public void requestInitialized(ServletRequestEvent arg0) {
	originalCL = Thread.currentThread().getContextClassLoader();

	IAmbassadorGenerator storedGenerator =
		(IAmbassadorGenerator) arg0.getServletContext().getAttribute(AMBASSADOR_CLASS_GENERATOR);

	ClassLoader newCL = storedGenerator.getAmbassadorClassLoader(originalCL);
	Thread.currentThread().setContextClassLoader(newCL);
    }
}
