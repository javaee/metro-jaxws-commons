/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ContextBuilder.java
 */

package org.jvnet.jax_ws_commons.beans_generator;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.WebFault;

import org.jvnet.jax_ws_commons.beans_generator.ambassador.AmbassadorGenerationException;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.IAmbassadorGenerator;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.IWSExceptionInfo;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.IWSMethodInfo;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.WSImplemetatorAnnotationInfo;
import org.jvnet.jax_ws_commons.beans_generator.conf.IDeploymentConfigurationReader;
import org.jvnet.jax_ws_commons.beans_generator.conf.IEndpointData;
import org.jvnet.jax_ws_commons.beans_generator.invoker.IImplementationInvoker;

/**
 * Entry point into context subsystem.
 * 
 * Current limitation: It don't process method's argument's annotations for now.
 * 
 * Created: 08.06.2007
 * 
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class ContextBuilder {
    private IDeploymentConfigurationReader confReader = null;

    private IAmbassadorGenerator ambassadorGenerator = null;

    private Logger log = null;

    /**
         * @param confReaderClass
         * @param ambassadorGenerator
         */
    public ContextBuilder(IDeploymentConfigurationReader confReader,
	    IAmbassadorGenerator ambassadorGenerator) {
	super();
	this.log = Logger.getLogger(this.getClass().getCanonicalName());
	this.confReader = confReader;
	this.ambassadorGenerator = ambassadorGenerator;
    }

    /**
         * Build all necessary artifacts.
     * @throws ContextSubsystemException 
         * 
         * @throws ContextSubsystemException
         */
    public void build() throws ContextSubsystemException {
	try {
	    IEndpointData data = confReader.readConfiguration();
	    for (IEndpointData.EndpointConfiguration conf : data
		    .getEndpointPairs()) {

		String implementatorClass = (conf.getWrappedClassName() != null) ? conf
			.getWrappedClassName()
			: conf.getInvokableClassName();

		WSImplemetatorAnnotationInfo implWsInfo = constructWSImplemetatorAnnotationInfo(implementatorClass);

		Class<? extends IImplementationInvoker> invokerClass = null;
		if (conf.isNeedToWrap())
		    invokerClass = createInitializedClass(conf
			    .getInvokerClassName());
		ambassadorGenerator.generateAndLoadClasses(conf
			.getWrappedClassName(), conf.getInvokableClassName(),
			implWsInfo, invokerClass, confReader.getClass(), data);
	    }
	} catch (ClassNotFoundException e) {
	    throw new ContextSubsystemException(e);
	}
    }

    /**
         * @param classPairs
         * @return
         */
    /*
         * protected <K, V> List<K> getKeys(List<Pair<K, V>> classPairs) {
         * List<K> result = new ArrayList<K>(classPairs.size()); for (Pair<K,
         * V> pair : classPairs) result.add(pair.Key); return result; }
         */

    /**
         * Create uninitialized class instance to prevent static initilizer
         * invocation.
         * 
         * @param className
         * @return
         * @throws ClassNotFoundException
         */
    protected Class createUninitializedClass(String className)
	    throws ClassNotFoundException {
	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	return Class.forName(className, false, cl);
    }

    protected Class createInitializedClass(String className)
	    throws ClassNotFoundException {
	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	return Class.forName(className, true, cl);
    }

    /**
         * Construct information abbout existing annotations of invocable class
         * (in case of its existence)
         * 
         * 
         * @param codeAR -
         *                code analyse results
         * @param wrappedClass -
         *                wrapped class by 'ambassador' or null (in case if we
         *                don't want to wrap)
         * @param invokableClassName -
         *                class name of invokable class
         * @return
         * @throws ContextSubsystemException
         * @throws ClassNotFoundException
     * @throws ContextSubsystemException 
         */
    protected WSImplemetatorAnnotationInfo constructWSImplemetatorAnnotationInfo(
	    String className) throws ClassNotFoundException, ContextSubsystemException {
	WSImplemetatorAnnotationInfo result = new WSImplemetatorAnnotationInfo();

	Class clazz = createUninitializedClass(className);

	// check for javax.jws.WebService annotation on class
	javax.jws.WebService webServiceAnn = (javax.jws.WebService) clazz
		.getAnnotation(javax.jws.WebService.class);
	result.setTargetNS(ContextJAXWSUtils.generateTargetNSFromClass(clazz));
	result.setName(clazz.getSimpleName());
	result.setServiceName(clazz.getSimpleName());
	if (null != webServiceAnn) {
	    result.setTargetNS(ContextUtils.getAnnValue(webServiceAnn
		    .targetNamespace(), result.getTargetNS()));
	    result.setName(ContextUtils.getAnnValue(webServiceAnn.name(),
		    result.getName()));
	    result.setServiceName(ContextUtils.getAnnValue(webServiceAnn
		    .serviceName(), result.getServiceName()));
	} else {
	    throw new JAXWSInvalidAnnotationException(
		    "Class for processing must be annotated with "
			    + javax.jws.WebService.class.getCanonicalName()
			    + " annotation.");
	}

	// TODO: add checking for web-service wrapping type
	// TODO: checking for method's access modifiers

	// get methods and exceptions for processing
	Collection<Method> mds = Arrays.asList(clazz.getMethods());
	List<IWSMethodInfo> methodsForProcessing = new ArrayList<IWSMethodInfo>(
		mds.size());
	Map<String, IWSExceptionInfo> exceptionsForProcessing = new LinkedHashMap<String, IWSExceptionInfo>(
		mds.size());
	for (Method md : mds) {

	    IWSMethodInfo mi = createWSMethodInfo(result, md, clazz);
	    if (null != mi) {
		methodsForProcessing.add(mi);

		// if methods processed - process exceptions
		for (Class ex : md.getExceptionTypes()) {
		    if (!exceptionsForProcessing.containsKey(ex.toString())) {
			exceptionsForProcessing.put(ex.toString(),
				createWSExceptionInfo(result, ex, clazz));
		    }
		}
	    }
	}
	result.setMethodInfos(methodsForProcessing);
	result.setExceptionInfos(exceptionsForProcessing.values());

	return result;
    }

    protected IWSMethodInfo createWSMethodInfo(
	    WSImplemetatorAnnotationInfo parentHolder, Method md,
	    Class methodHolderClass) {
	String reqClassName, resClassName, reqNS, resNS, reqName, resName;

	javax.jws.WebMethod wmAnn = md.getAnnotation(javax.jws.WebMethod.class);
	if (wmAnn != null) {
	    if (wmAnn.exclude()) {
		log
			.info("Method "
				+ md
				+ " was manualy excluded from list of processed methods.");
		return null;
	    }
	    log.info("Method " + md
		    + " was added in list of processed methods.");

	    // check for RequestWrapper annotation
	    javax.xml.ws.RequestWrapper reqAnnWrapper = md
		    .getAnnotation(javax.xml.ws.RequestWrapper.class);

	    reqClassName = ContextJAXWSUtils.generateRequestWrapperClassName(
		    md, methodHolderClass);
	    reqNS = parentHolder.getTargetNS();
	    reqName = ContextJAXWSUtils.generateRequestWrapperName(md);

	    if (reqAnnWrapper != null) {
		reqName = ContextUtils.getAnnValue(reqAnnWrapper.localName(),
			reqName);
		reqClassName = ContextUtils.getAnnValue(reqAnnWrapper
			.className(), reqClassName);
		reqNS = ContextUtils.getAnnValue(reqAnnWrapper
			.targetNamespace(), reqNS);
	    }

	    // check for ResponseWrapper annotation
	    javax.xml.ws.ResponseWrapper resAnnWrapper = md
		    .getAnnotation(javax.xml.ws.ResponseWrapper.class);
	    resClassName = ContextJAXWSUtils.generateResponseWrapperClassName(
		    md, methodHolderClass);
	    resNS = parentHolder.getTargetNS();
	    resName = ContextJAXWSUtils.generateResponseWrapperName(md);
	    if (resAnnWrapper != null) {
		resName = ContextUtils.getAnnValue(resAnnWrapper.localName(),
			resName);
		resClassName = ContextUtils.getAnnValue(resAnnWrapper
			.className(), resClassName);
		resNS = ContextUtils.getAnnValue(resAnnWrapper
			.targetNamespace(), resNS);
	    }

	    // creating method info object
	    IWSMethodInfo mi = ambassadorGenerator.constructMethodInfo(md);
	    // ... and setting attributes
	    mi.setRequestBeanClassName(reqClassName);
	    mi.setRequestName(reqName);
	    mi.setRequestNS(reqNS);
	    mi.setResponseBeanClassName(resClassName);
	    mi.setResponseName(resName);
	    mi.setResponseNS(resNS);
	    return mi;
	} // if (wmAnn != null) {
	return null;
    }

    protected IWSExceptionInfo createWSExceptionInfo(
	    WSImplemetatorAnnotationInfo parentHolder, Class exceptionClass,
	    Class parentClass) {
	String fbClassName = null;
	String fbNS = null, fbName = null;

	fbClassName = ContextJAXWSUtils.generateExceptionWrapperClassName(
		exceptionClass, parentClass);
	fbName = exceptionClass.getName();
	fbNS = ContextJAXWSUtils.generateTargetNSFromClass(exceptionClass);

	if (RuntimeException.class.isAssignableFrom(exceptionClass)
		|| RemoteException.class.isAssignableFrom(exceptionClass))
	    return null;

	WebFault wfAnn = (WebFault) exceptionClass
		.getAnnotation(WebFault.class);
	if (null != wfAnn) {
	    fbClassName = ContextUtils.getAnnValue(wfAnn.faultBean(),
		    fbClassName);
	    fbNS = ContextUtils.getAnnValue(wfAnn.targetNamespace(), fbNS);
	    fbName = ContextUtils.getAnnValue(wfAnn.name(), fbName);

	    // check: is there "getFaultInfo" method? (skip if it exists)
	    try {
		if (null != exceptionClass.getMethod("getFaultInfo",
			new Class[] {}))
		    return null;
	    } catch (SecurityException e) {
		log.log(Level.SEVERE,
			"Exception while checking for method 'getFaultInfo' in class '"
				+ exceptionClass.getCanonicalName()
				+ "' - skip it.", e);
		return null;
	    } catch (NoSuchMethodException e) {
		// do nothing
	    }
	}

	IWSExceptionInfo res = ambassadorGenerator
		.constructExceptionInfo(exceptionClass);
	res.setClassName(fbClassName);
	res.setName(fbName);
	res.setNs(fbNS);
	return res;
    }
}
