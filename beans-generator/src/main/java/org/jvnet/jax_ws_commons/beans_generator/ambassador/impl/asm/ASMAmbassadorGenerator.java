/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ASMAmbassadorGenerator.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador.impl.asm;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jvnet.jax_ws_commons.beans_generator.ambassador.AmbassadorGenerationException;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.IAmbassadorGenerator;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.IWSExceptionInfo;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.IWSMethodInfo;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.WSImplemetatorAnnotationInfo;
import org.jvnet.jax_ws_commons.beans_generator.conf.IEndpointData;
import org.jvnet.jax_ws_commons.beans_generator.invoker.IImplementationInvoker;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;


/**
 * Implementation ambassador generator (ASM-specific realization) .
 * 
 * @see org.jvnet.jax_ws_commons.beans_generator.ambassador.IAmbassadorGenerator
 * 
 * Created: 04.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class ASMAmbassadorGenerator implements IAmbassadorGenerator {
    private Map<String, byte[]> classes = new HashMap<String, byte[]>(10);

    class AmbassadorClassLoader extends ClassLoader {

	public AmbassadorClassLoader(ClassLoader originalCL) {
	    super(originalCL);

	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
	    Class<?> result = null;
	    if (classes.containsKey(name)) {
		byte[] code = classes.get(name);
		System.out.println("AmbassadorClassLoader::generateClasses(): "
			+ name);

		// Looking up the package
		String packageName = null;
		int pos = name.lastIndexOf('.');
		if (pos != -1)
		    packageName = name.substring(0, pos);
		Package pkg = null;
		if (packageName != null) {
		    pkg = getPackage(packageName);
		    // Define the package (if null)
		    if (pkg == null)
			definePackage(packageName, null, null, null, null,
				null, null, null);
		}

		return defineClass(name, code, 0, code.length);
	    }
	    result = getParent().loadClass(name);
	    if (null == result)
		throw new ClassNotFoundException(name);
	    return result;
	}
    }

    /**
         * Created: 01.07.2007
         * 
         * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
         * @version $Revision$
         */
    static class ExceptionInfo implements IWSExceptionInfo {
	private String className = null;

	private String name = null;

	private String ns = null;

	private String wrappedClassName = null;

	/**
         * @param qualifiedName
         */
	public ExceptionInfo(String wrappedClassName) {
	    this.wrappedClassName = wrappedClassName;
	}

	/**
         * @param wrappedClassName
         * @param className
         * @param name
         * @param ns
         */
	public ExceptionInfo(String originalClassName, String className,
		String name, String ns) {
	    super();
	    this.wrappedClassName = originalClassName;
	    this.className = className;
	    this.name = name;
	    this.ns = ns;
	}

	/**
         * @return the className
         */
	public String getClassName() {
	    return this.className;
	}

	/**
         * @return the name
         */
	public String getName() {
	    return this.name;
	}

	/**
         * @return the ns
         */
	public String getNs() {
	    return this.ns;
	}

	/**
         * @param className
         *                the className to set
         */
	public void setClassName(String className) {
	    this.className = className;
	}

	/**
         * @param name
         *                the name to set
         */
	public void setName(String name) {
	    this.name = name;
	}

	/**
         * @param ns
         *                the ns to set
         */
	public void setNs(String ns) {
	    this.ns = ns;
	}

	public String getOriginalClassName() {
	    return this.wrappedClassName;
	}

    }

    /**
         * Method-data holder.
         * 
         * Created: 06.06.2007
         * 
         * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
         * @version $Revision: 240 $
         */
    static class MethodInfo implements IWSMethodInfo {
	private String requestName, requestBeanClassName, responseName,
		responseBeanClassName, requestNS, responseNS;

	private int access;

	private String name;

	private String desc;

	private Type[] argTypes;

	private Type returnType;

	private String signature;

	private String id;

	private String[] exceptions;

	public void addInternalData(int access, String name, String desc,
		String signature, String[] exceptions) {
	    this.access = access;
	    this.name = name;
	    setDesc(desc);
	    this.signature = signature;
	    this.exceptions = exceptions;
	}

	/**
         * For testing targets.
         * 
         * @param requestName
         * @param requestBeanClassName
         * @param requestNS
         * @param responseName
         * @param responseBeanClassName
         * @param responseNS
         */
	public MethodInfo(String requestName, String requestBeanClassName,
		String requestNS, String responseName,
		String responseBeanClassName, String responseNS) {
	    this.requestName = requestName;
	    this.requestBeanClassName = requestBeanClassName;
	    this.responseName = responseName;
	    this.responseBeanClassName = responseBeanClassName;
	    this.requestNS = requestNS;
	    this.responseNS = responseNS;
	}

	/**
         * 
         */
	public MethodInfo(String id) {
	    super();
	    this.id = id;
	}

	public int getAccess() {
	    return this.access;
	}

	public String getDesc() {
	    return this.desc;
	}

	public String[] getExceptions() {
	    return this.exceptions;
	}

	public String getName() {
	    return this.name;
	}

	public String getSignature() {
	    return this.signature;
	}

	public Type[] getArgTypes() {
	    return this.argTypes;
	}

	public Type getReturnType() {
	    return this.returnType;
	}

	void setDesc(String desc) {
	    this.desc = desc;
	    argTypes = Type.getArgumentTypes(desc);
	    returnType = Type.getReturnType(desc);

	}

	void setExceptions(String[] exceptions) {
	    this.exceptions = exceptions;
	}

	// IWSMethodInfo method implementations
	public String getRequestBeanClassName() {
	    return this.requestBeanClassName;
	}

	public void setRequestBeanClassName(String requestBeanClassName) {
	    this.requestBeanClassName = requestBeanClassName;
	}

	public String getRequestName() {
	    return this.requestName;
	}

	public void setRequestName(String requestName) {
	    this.requestName = requestName;
	}

	public String getRequestNS() {
	    return this.requestNS;
	}

	public void setRequestNS(String requestNS) {
	    this.requestNS = requestNS;
	}

	public String getResponseBeanClassName() {
	    return this.responseBeanClassName;
	}

	public void setResponseBeanClassName(String responseBeanClassName) {
	    this.responseBeanClassName = responseBeanClassName;
	}

	public String getResponseName() {
	    return this.responseName;
	}

	public void setResponseName(String responseName) {
	    this.responseName = responseName;
	}

	public String getResponseNS() {
	    return this.responseNS;
	}

	public void setResponseNS(String responseNS) {
	    this.responseNS = responseNS;
	}

	public String getId() {
	    return this.id;
	}
    }

    public void generateAndLoadClasses(String wrappedClassName,
	    String invocableClassName, WSImplemetatorAnnotationInfo implInfo,
	    Class<? extends IImplementationInvoker> implementationInvokerClass,
	    Class confReaderClass, IEndpointData endpointData)
	    throws AmbassadorGenerationException {

	// we decide here: "Generate or don't generate wrapped class?"
	if (null != wrappedClassName) {

	    try {
		byte[] res = createWSWrapperClass(classes, wrappedClassName,
			invocableClassName, implInfo,
			implementationInvokerClass, confReaderClass);
		classes.put(invocableClassName, res);
	    } catch (Exception e) {
		throw new AmbassadorGenerationException(
			"Exception while generating 'ambassador' class.", e);
	    }

	} else {
	    try {
		analyseWSImplementationClass(invocableClassName, implInfo);
	    } catch (Exception e) {
		throw new AmbassadorGenerationException(
			"Exception while analysing original class.", e);
	    }

	}
	try {
	    // TODO: add checking for web-service wrapping type
	    // if (implInfo.getMappingType() ==
	    // WSImplemetatorAnnotationInfo.MappingType.DOC_WRAPPED)
	    createWSArtifacts(classes, implInfo);
	} catch (Exception e) {
	    throw new AmbassadorGenerationException(
		    "Exception while generating JAX-WS artifacts (request/response beans).",
		    e);
	}

	try {
	    // TODO: add checking for web-service wrapping type
	    // if (implInfo.getMappingType() ==
	    // WSImplemetatorAnnotationInfo.MappingType.DOC_WRAPPED)
	    createWSFaultBeans(classes, implInfo);
	} catch (Exception e) {
	    throw new AmbassadorGenerationException(
		    "Exception while generating JAX-WS artifacts (fault beans).",
		    e);
	}

    }

    /**
         * @param invocableClassName
         * @param implInfo
         * @throws IOException
         */
    private void analyseWSImplementationClass(String invocableClassName,
	    WSImplemetatorAnnotationInfo implInfo) throws IOException {
	ClassReader reader = ASMUtil.createClassReader(invocableClassName);
	ClassVisitor cv = new ASMAmbassadorInvokableClassAnalyst(implInfo);
	reader.accept(cv, ClassReader.SKIP_CODE & ClassReader.SKIP_DEBUG
		& ClassReader.SKIP_FRAMES);
    }

    /**
         * Create additional web-servce artifacts.
         * 
         * @param implInfo
         */
    protected void createWSFaultBeans(Map<String, byte[]> classes,
	    WSImplemetatorAnnotationInfo implInfo) {
	for (IWSExceptionInfo ei : implInfo.getExceptionInfos()) {
	    byte[] ec = createFaultBean(ei);
	    classes.put(ei.getClassName(), ec);
	}
    }

    /**
         * @param ei
         * @return
         */
    protected byte[] createFaultBean(IWSExceptionInfo ei) {
	ASMFaultBeanGenerator gen = new ASMFaultBeanGenerator(
		(ExceptionInfo) ei);
	return gen.generate();
    }

    /**
         * Create additional web-servce artifacts.
         * 
         * @param implInfo
         */
    protected void createWSArtifacts(Map<String, byte[]> classes,
	    WSImplemetatorAnnotationInfo implInfo) {
	for (IWSMethodInfo mi : implInfo.getMethodInfos()) {
	    byte[] res = createResponseBean(mi);
	    byte[] req = createRequestBean(mi);
	    classes.put(mi.getResponseBeanClassName(), res);
	    classes.put(mi.getRequestBeanClassName(), req);
	}
    }

    /**
         * @param mi
         */
    protected byte[] createRequestBean(IWSMethodInfo mi) {
	ASMRequestBeanGenerator gen = new ASMRequestBeanGenerator(
		(MethodInfo) mi);
	return gen.generate();
    }

    /**
         * @param mi
         */
    protected byte[] createResponseBean(IWSMethodInfo mi) {
	ASMResponseBeanGenerator gen = new ASMResponseBeanGenerator(
		(MethodInfo) mi);
	return gen.generate();
    }

    /**
         * Create main web-service class.
         * 
         * @throws IOException
         * 
         */
    protected byte[] createWSWrapperClass(
	    Map<String, byte[]> classes,
	    String wrappedClassName,
	    String invokableClassName,
	    WSImplemetatorAnnotationInfo implInfo,
	    Class<? extends IImplementationInvoker> implementatationInvokerClass,
	    Class confReaderClass) throws IOException {
	ClassReader reader = ASMUtil.createClassReader(wrappedClassName);
	ClassWriter writer = ASMUtil.createClassWriter();
	ClassVisitor cv = new ASMAmbassadorWrapperClassCreator(
		implementatationInvokerClass, confReaderClass, implInfo,
		writer, wrappedClassName, invokableClassName);
	reader.accept(cv, ClassReader.SKIP_CODE & ClassReader.SKIP_DEBUG
		& ClassReader.SKIP_FRAMES);
	return writer.toByteArray();
    }

    public IWSMethodInfo constructMethodInfo(Method md) {
	String id = md.getName() + "(";
	for (Class param : md.getParameterTypes()) {
	    id += Type.getDescriptor(param);
	}
	id += ")";
	if (md.getReturnType() != null)
	    id += Type.getDescriptor(md.getReturnType());

	return new MethodInfo(id);
    }

    public IWSExceptionInfo constructExceptionInfo(Class cd) {
	return new ExceptionInfo(cd.getCanonicalName());
    }

    public ClassLoader getAmbassadorClassLoader(ClassLoader originalCL) {
	ClassLoader result = new AmbassadorClassLoader(originalCL);
	return result;
    }
}
