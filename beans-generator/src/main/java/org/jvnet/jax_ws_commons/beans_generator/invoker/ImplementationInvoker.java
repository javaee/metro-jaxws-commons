/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ImplementationInvoker.java
 */

package org.jvnet.jax_ws_commons.beans_generator.invoker;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.jax_ws_commons.beans_generator.conf.IDeploymentConfigurationReader;


/**
 * Class, directly invokable from ambassador.
 * Servers as dispather for all invokations of ws implementator. Simplest realization 
 * could simple invoke implementation, more sophisticated - make a lot of things.
 * 
 * Created: 04.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class ImplementationInvoker implements IImplementationInvoker {
    private IDeploymentConfigurationReader configReader = null;

    private String implClassName = null;

    private Class implClass = null;

    private Object impl = null;

    private Logger log = null;

    public void initialize(IDeploymentConfigurationReader configReader,
	    String implementationClassName) {
	this.configReader = configReader;
	this.implClassName = implementationClassName;
	this.log = Logger.getLogger(implementationClassName);
    }

    public Object invoke(String methodName, Class returnClass,
	    Class[] argumentClasses, Class[] thrownTypes, Object returnValue,
	    Object[] argumentValues) throws Exception {

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	PrintWriter pw = new PrintWriter(baos);
	pw.println("Invoking method: " + methodName);

	pw.println(">>> args count: " + argumentClasses.length);
	pw.println(">>> arg classes: ");
	for (Class clazz : argumentClasses)
	    pw.print(clazz.getSimpleName() + " ");
	pw.println();

	pw.println(">>> arg value: ");
	for (Object arg : argumentValues)
	    pw.print((arg != null ? arg.toString() : "null") + " ");
	pw.println();

	pw.println(">>> return type: " + returnClass.getSimpleName());

	pw.println(">>> thrown types count: " + thrownTypes.length);
	pw.println(">>> thrown type classes: ");
	for (Class clazz : thrownTypes)
	    pw.print(clazz.getSimpleName() + " ");
	pw.println();

	pw.flush();
	log.info(baos.toString());

	returnValue = invokeImplementation(methodName, returnClass, argumentClasses, thrownTypes, returnValue, argumentValues);

	log.info("Result: " + (returnValue != null ? returnValue.toString() : "null"));
	return returnValue;
    }

    /**
     * @param methodName
     * @param returnClass
     * @param argumentClasses
     * @param thrownTypes
     * @param returnValue
     * @param argumentValues
     */
    protected Object invokeImplementation(String methodName, Class returnClass,
	    Class[] argumentClasses, Class[] thrownTypes, Object returnValue,
	    Object[] argumentValues) throws Exception {

	// we create new array of args because we added on additional arg - String
	Class[] realArguments = new Class[argumentClasses.length - 1];
	System.arraycopy(argumentClasses, 0, realArguments, 0, argumentClasses.length - 1);
	Method method = null;

	try {
	    method = getMethod(methodName, realArguments);
	} catch (Exception e) {
	    log.log(Level.SEVERE, "Exception while getting implementator's method", e);
	}

	// do the same thing with arg values
	Object[] realArgValues = new Object[argumentValues.length - 1];
	System.arraycopy(argumentValues, 0, realArgValues, 0, argumentClasses.length - 1);
	//try {
	returnValue = method.invoke(getImplementationInstance(), realArgValues);
	//} catch (Exception e) {
	//log.log(Level.SEVERE, "Exception while invoking implementator", e);
	//}

	return returnValue;
    }

    /**
     * @param methodName
     * @param realArguments
     * @return
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @throws ClassNotFoundException 
     */
    protected Method getMethod(String methodName, Class[] realArguments)
	    throws SecurityException, NoSuchMethodException,
	    ClassNotFoundException {
	if (implClass == null)
	    implClass = getImplementationClass(implClassName);
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
    protected Class<?> getImplementationClass(String className)
	    throws ClassNotFoundException {
	Class<?> result = null;
	if (result == null) {
	    log.info("Creating class instance of " + implClassName);
	    result = Class.forName(className);
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
    protected Object getImplementationInstance() throws InstantiationException,
	    IllegalAccessException {
	if (impl == null) {
	    log.info("Creating instance of " + implClassName);
	    impl = implClass.newInstance();
	}
	return impl;
    }

}
