/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: MagnetContextInvoker.java
 */

package org.jvnet.jax_ws_commons.beans_generator.invoker;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jvnet.jax_ws_commons.beans_generator.conf.IDeploymentConfigurationReader;


/**
 * Created: 18.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision$
 */
public class MagnetContextInvoker implements IImplementationInvoker {
    private static Map<String, MagnetContext> contexts =
	    new HashMap<String, MagnetContext>(50);

    private IDeploymentConfigurationReader configReader = null;

    private String implementationClass = null;

    private Logger log = null;

    public MagnetContextInvoker() {
	super();
	// TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.jvnet.jax_ws_commons.beans_generator.invoker.IImplementationInvoker#initialize(org.jvnet.jax_ws_commons.beans_generator.conf.IDeploymentConfigurationReader, java.lang.Class)
     */
    public void initialize(IDeploymentConfigurationReader configReader,
	    String implementationClass) {
	this.implementationClass = implementationClass;
	this.configReader = configReader;
	this.log = Logger.getLogger(implementationClass);
    }

    /* (non-Javadoc)
     * @see org.jvnet.jax_ws_commons.beans_generator.invoker.IImplementationInvoker#invoke(java.lang.String, java.lang.Class, java.lang.Class[], java.lang.Class[], java.lang.Object, java.lang.Object[])
     */
    public Object invoke(String methodName, Class returnClass,
	    Class[] argumentClasses, Class[] thrownTypes, Object returnValue,
	    Object[] argumentValues) {
	MagnetContext context = null;
	String contextName = (String) argumentValues[argumentValues.length - 1];
	if (null != contextName && !contextName.trim().equals("")) {
	    context = getContext(contextName);
	    return context.invokeImplementation(methodName, returnClass, argumentClasses, thrownTypes, returnValue, argumentValues);
	} else {
	    log.severe("Not valid context name: " + contextName);
	    return null;
	}
    }

    /**
     * Get context by name.
     * 
     * Creates new instance in abscence cese.
     * 
     * @param contextName
     * @return
     */
    protected MagnetContext getContext(String contextName) {
	MagnetContext result = contexts.get(contextName);
	if (null == result) {
	    synchronized (contexts) {
		result =
			new MagnetContext(contextName, this, implementationClass);
		contexts.put(contextName, result);
	    }
	}
	return result;
    }

}
