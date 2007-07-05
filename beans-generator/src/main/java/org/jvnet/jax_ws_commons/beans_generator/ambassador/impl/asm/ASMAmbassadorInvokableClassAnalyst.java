/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ASMAmbassadorInvokableClassAnalyst.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador.impl.asm;

import java.util.logging.Logger;

import org.jvnet.jax_ws_commons.beans_generator.ambassador.IWSMethodInfo;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.WSImplemetatorAnnotationInfo;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.impl.asm.ASMAmbassadorGenerator.MethodInfo;
import org.jvnet.jax_ws_commons.beans_generator.conf.IDeploymentConfigurationReader;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Created: 05.07.2007
 * 
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision$
 */
public class ASMAmbassadorInvokableClassAnalyst implements ClassVisitor,
	Opcodes {

    private WSImplemetatorAnnotationInfo implInfo = null;

    private Logger log = null;

    /**
         * Create new instance of class analyst.
         * 
         * @param confReaderClass
         *                deployment-specific configuration reader class
         * @param implInfo
         *                instance WSImplemetatorAnnotationInfo for original
         *                class
         * @param cv
         *                ASM class writer
         * @param wrappedClassName
         *                fully quallified original class name
         * @param invokableClassName
         *                fully quallified generated class name
         * 
         * @see IDeploymentConfigurationReader
         */
    public ASMAmbassadorInvokableClassAnalyst(
	    WSImplemetatorAnnotationInfo implInfo) {
	this.implInfo = implInfo;
    }

    public void visit(int version, int access, String name, String signature,
	    String superName, String[] interfaces) {
    }

    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
	return null;
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitAttribute(org.objectweb.asm.Attribute)
         */
    public void visitAttribute(Attribute arg0) {
	// TODO Auto-generated method stub

    }

    public void visitEnd() {
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitField(int, java.lang.String,
         *      java.lang.String, java.lang.String, java.lang.Object)
         */
    public FieldVisitor visitField(int arg0, String arg1, String arg2,
	    String arg3, Object arg4) {
	// TODO Auto-generated method stub
	return null;
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitInnerClass(java.lang.String,
         *      java.lang.String, java.lang.String, int)
         */
    public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
	// TODO Auto-generated method stub

    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitMethod(int,
         *      java.lang.String, java.lang.String, java.lang.String,
         *      java.lang.String[])
         */
    public MethodVisitor visitMethod(int access, String name, String desc,
	    String signature, String[] exceptions) {

	// fill method with necessary data for generation
	MethodInfo md = getMethodInfoFromImplInfo(access, name, desc,
		signature, exceptions);
	if (null != md)
	    md.addInternalData(access, name, desc, signature, exceptions);

	// we don't create method instances
	return null;
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitOuterClass(java.lang.String,
         *      java.lang.String, java.lang.String)
         */
    public void visitOuterClass(String arg0, String arg1, String arg2) {
	// TODO Auto-generated method stub

    }

    /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.ClassVisitor#visitSource(java.lang.String,
         *      java.lang.String)
         */
    public void visitSource(String arg0, String arg1) {
	// TODO Auto-generated method stub

    }

    protected MethodInfo getMethodInfoFromImplInfo(int access, String name,
	    String desc, String signature, String[] exceptions) {
	for (IWSMethodInfo mi : implInfo.getMethodInfos()) {
	    MethodInfo result = (MethodInfo) mi;

	    if (result.getId().equals(name + desc)) {
		return result;
	    }
	}
	return null;
    }

}
