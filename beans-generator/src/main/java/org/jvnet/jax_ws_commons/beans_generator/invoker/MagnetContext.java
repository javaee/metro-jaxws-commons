/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: MagnetContext.java
 */

package org.jvnet.jax_ws_commons.beans_generator.invoker;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.jax_ws_commons.beans_generator.ContextUtils;


/**
 * Created: 18.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision$
 */
class MagnetContext {

    /**
     * Class containig class switching data: original and current class loader.
     * 
     * Created: 19.06.2007
     * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
     * @version $Revision$
     */
    private static class SwitchClassLoaderData {
	public ClassLoader currentClassLoader = null;

	public ClassLoader originalClassLoader = null;

	/**
	 * @param originalClassLoader
	 * @param currentClassLoader
	 */
	public SwitchClassLoaderData(ClassLoader originalClassLoader,
		ClassLoader currentClassLoader) {
	    super();
	    this.originalClassLoader = originalClassLoader;
	    this.currentClassLoader = currentClassLoader;
	}
    }

    private String implClassName = null;

    private Class implClass = null;

    private Object impl = null;

    private Logger log = null;

    //private MagnetContextBaseClassLoader contextCL = null;

    private MagnetContextInvoker holder = null;

    private String contextName = null;

    /**
     * @param contextName
     * @param holder
     * @param implClassName
     */
    public MagnetContext(String contextName, MagnetContextInvoker holder,
	    String implClassName) {
	super();
	this.contextName = contextName;
	this.holder = holder;
	this.implClassName = implClassName;
	this.log = Logger.getLogger(implClassName + ".context=" + contextName);
	//ClassLoader cl = Thread.currentThread().getContextClassLoader();
	//this.contextCL = new MagnetContextBaseClassLoader(cl);
    }

    /**
     * @param methodName
     * @param returnClass
     * @param argumentClasses
     * @param thrownTypes
     * @param returnValue
     * @param argumentValues
     */
    public Object invokeImplementation(String methodName, Class returnClass,
	    Class[] argumentClasses, Class[] thrownTypes, Object returnValue,
	    Object[] argumentValues) {

	SwitchClassLoaderData swCL = null;
	try {
	    swCL = plugInClassLoader();

	    // we create new array of args because we added on additional arg - String
	    Class[] realArguments = new Class[argumentClasses.length - 1];
	    System.arraycopy(argumentClasses, 0, realArguments, 0, argumentClasses.length - 1);
	    Method method = null;

	    try {
		method = getMethod(methodName, realArguments, swCL);
	    } catch (Exception e) {
		log.log(Level.SEVERE, "Exception while getting implementator's method", e);
	    }

	    // do the same thing with arg values
	    Object[] realArgValues = new Object[argumentValues.length - 1];
	    System.arraycopy(argumentValues, 0, realArgValues, 0, argumentClasses.length - 1);
	    try {
		returnValue = method.invoke(getImplementationInstance(swCL), realArgValues);
		if (returnValue == null)
		    returnValue = ContextUtils.createDefaultReturnValueByClass(returnClass);
	    } catch (Exception e) {
		log.log(Level.SEVERE, "Exception while invoking implementator", e);
	    }

	    return returnValue;
	} finally {
	    plugOutClassLoader(swCL);
	}
    }

    /**
     * @param methodName
     * @param realArguments
     * @return
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @throws ClassNotFoundException 
     */
    protected Method getMethod(String methodName, Class[] realArguments,
	    SwitchClassLoaderData data) throws SecurityException,
	    NoSuchMethodException, ClassNotFoundException {
	if (implClass == null)
	    implClass = getImplementationClass(implClassName, data);
	return implClass.getMethod(methodName, realArguments);
    }

    /**
     * Getting or creating implementation class instance.
     * We use current substituted class loader for creating class instance. 
     * 
     * @param className
     * @param data
     * @return
     * @throws ClassNotFoundException
     */
    protected Class<?> getImplementationClass(String className,
	    SwitchClassLoaderData data) throws ClassNotFoundException {
	Class<?> result = null;
	if (result == null) {
	    log.info("Creating class instance of " + implClassName);
	    result = Class.forName(className, true, data.currentClassLoader);
	}
	return result;
    }

    /**
     * Get or create if necessary implementation instance.
     * 
     * We don't check existence of implementation class instance - because we already checked 
     * it previously ({@link #getMethod}).
     * 
     * @param data
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    protected Object getImplementationInstance(SwitchClassLoaderData data)
	    throws InstantiationException, IllegalAccessException {
	if (impl == null) {
	    log.info("Creating instance of " + implClassName);
	    impl = implClass.newInstance();
	}
	return impl;
    }

    protected SwitchClassLoaderData plugInClassLoader() {
	ClassLoader originalCL = Thread.currentThread().getContextClassLoader();
	MagnetContextClassLoader newCL = new MagnetContextClassLoader(contextName, originalCL);
	Thread.currentThread().setContextClassLoader(newCL);
	return new SwitchClassLoaderData(originalCL, newCL);
    }

    protected void plugOutClassLoader(SwitchClassLoaderData data) {
	Thread.currentThread().setContextClassLoader(data.originalClassLoader);
    }
}
