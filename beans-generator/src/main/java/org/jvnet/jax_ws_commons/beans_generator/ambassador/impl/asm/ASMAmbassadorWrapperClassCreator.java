/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ASMAmbassadorWrapperClassCreator.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador.impl.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jvnet.jax_ws_commons.beans_generator.ambassador.IWSMethodInfo;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.WSImplemetatorAnnotationInfo;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.impl.asm.ASMAmbassadorGenerator.MethodInfo;
import org.jvnet.jax_ws_commons.beans_generator.conf.IDeploymentConfigurationReader;
import org.jvnet.jax_ws_commons.beans_generator.invoker.IImplementationInvoker;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


/**
 * Classs which creates 'ambassador' class.
 * 
 * 
 * Created: 04.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
@SuppressWarnings("unused")
class ASMAmbassadorWrapperClassCreator extends ClassAdapter implements Opcodes {

    private List<MethodInfo> methodDatas = new ArrayList<MethodInfo>(10);

    private Logger log = null;

    private WSImplemetatorAnnotationInfo implInfo = null;

    private Class implmentatationInvokerClass = null;

    private String invokableClassName = null;

    private Class confReaderClass = null;

    private String wrappedClassName;

    public void visit(int version, int access, String name, String signature,
	    String superName, String[] interfaces) {
	cv.visit(49, access, invokableClassName.replace(".", "/"), signature, superName, interfaces);
	processAnnotations(cv);

    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassAdapter#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
	return null;
    }

    /**
     * @param cv
     */
    private void processAnnotations(ClassVisitor cv) {
	AnnotationVisitor av =
		cv.visitAnnotation(Type.getDescriptor(javax.jws.WebService.class), true);
	av.visit("targetNamespace", implInfo.getTargetNS());
	av.visit("name", implInfo.getName());
	av.visit("serviceName", implInfo.getServiceName());
	av.visitEnd();
    }

    /**
     * @param arg0
     */
    public ASMAmbassadorWrapperClassCreator(ClassVisitor cv) {
	super(cv);
	log =
		Logger.getLogger(this.getClass().getCanonicalName());
    }

    /**
     * Create new instance of class generator.
     * 
     * @param confReaderClass deployment-specific configuration reader class 
     * @param implInfo instance WSImplemetatorAnnotationInfo for original class
     * @param cv ASM class writer 
     * @param wrappedClassName fully quallified original class name
     * @param invokableClassName fully quallified generated class name
     * 
     * @see IDeploymentConfigurationReader
     */
    public ASMAmbassadorWrapperClassCreator(
	    Class<? extends IImplementationInvoker> implmentatationInvokerClass,
	    Class confReaderClass, WSImplemetatorAnnotationInfo implInfo,
	    ClassVisitor cv, String wrappedClassName, String invokableClassName) {
	this(cv);
	this.wrappedClassName = wrappedClassName;
	this.invokableClassName = invokableClassName;
	this.implmentatationInvokerClass = implmentatationInvokerClass;
	this.confReaderClass = confReaderClass;
	this.implInfo = implInfo;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    public MethodVisitor visitMethod(int access, String name, String desc,
	    String signature, String[] exceptions) {

	//log.info("Visited method " + name);
	if ("<init>".equalsIgnoreCase(name)) {
	    // create delegator
	    MethodVisitor mv =
		    cv.visitMethod(access, name, desc, signature, exceptions);
	    return new ASMAmbassadorWrapperClassConstructorCreator(mv, implmentatationInvokerClass, confReaderClass, invokableClassName, wrappedClassName);
	}

	// fill method with necessary data for generation
	MethodInfo md =
		getMethodInfoFromImplInfo(access, name, desc, signature, exceptions);
	if (null != md) {
	    // collect methods' information
	    md.addInternalData(access, name, desc, signature, exceptions);
	    methodDatas.add(md);
	}
	
	// we don't create method instances - we create our wrappers later
	return null;
    }

    /**
     * ѕолучить соответстувующию информаци€ о методе по данным анализа java кода.
     * 
     * @param access
     * @param name
     * @param desc
     * @param signature
     * @param exceptions
     * @return
     */
    protected MethodInfo getMethodInfoFromImplInfo(int access, String name,
	    String desc, String signature, String[] exceptions) {
	for (IWSMethodInfo mi : implInfo.getMethodInfos()) {
	    MethodInfo result = (MethodInfo) mi;

	    if (result.getId().equals(name + desc)) {
		//log.info("Found method for bytecode generation with signature " + result.getMethodUniqueIdentifier());
		return result;
	    }
	}
	return null;
    }

    @Override
    public void visitEnd() {
	// create necessary fields
	FieldVisitor fv =
		cv.visitField(ACC_PRIVATE, "invoker", Type.getDescriptor(IImplementationInvoker.class), null, null);
	if (null != fv) fv.visitEnd();
	fv =
		cv.visitField(ACC_PRIVATE, "configReader", Type.getDescriptor(IDeploymentConfigurationReader.class), null, null);
	if (null != fv) fv.visitEnd();

	// create method wrappers
	ASMAmbassadorWrapperMethodGenerator methodGenerator =
		new ASMAmbassadorWrapperMethodGenerator(cv, invokableClassName);
	for (MethodInfo methodData : methodDatas)
	    methodGenerator.generateMethod(methodData);

	super.visitEnd();
    }

}
