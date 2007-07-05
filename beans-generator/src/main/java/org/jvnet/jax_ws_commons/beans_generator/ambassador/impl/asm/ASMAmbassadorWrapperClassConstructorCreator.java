/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ASMAmbassadorWrapperClassConstructorCreator.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador.impl.asm;

import org.jvnet.jax_ws_commons.beans_generator.conf.IDeploymentConfigurationReader;
import org.jvnet.jax_ws_commons.beans_generator.invoker.IImplementationInvoker;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


/**
 * Created: 05.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
class ASMAmbassadorWrapperClassConstructorCreator implements MethodVisitor,
	Opcodes {

    private Class confReaderClass = null;

    private Class implmentationInvokerClass = null;

    private MethodVisitor mv = null;

    private String invokableClassName = null;

    private String wrappedClassName = null;

    /**
     * @param arg0
     */
    public ASMAmbassadorWrapperClassConstructorCreator(MethodVisitor mv) {
	this.mv = mv;
    }

    /**
     * @param arg0
     * @param classWriter
     * @param confReaderClass
     */
    public ASMAmbassadorWrapperClassConstructorCreator(MethodVisitor mv,
	    Class implmentatationInvokerClass, Class confReaderClass,
	    String invokableClassName, String wrappedClassName) {
	this(mv);
	this.confReaderClass = confReaderClass;
	this.invokableClassName = invokableClassName;
	this.wrappedClassName = wrappedClassName;
	this.implmentationInvokerClass = implmentatationInvokerClass;
    }

    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
	return mv.visitAnnotation(arg0, arg1);
    }

    public AnnotationVisitor visitAnnotationDefault() {
	return mv.visitAnnotationDefault();
    }

    public void visitAttribute(Attribute arg0) {
	// nothing to do
    }

    public void visitCode() {
	String invokableClassInternalName =
		invokableClassName.replace(".", "/");
	String invokableClassDescription =
		"L" + invokableClassName.replace(".", "/") + ";";

	mv.visitCode();
	Label l0 = new Label();
	mv.visitLabel(l0);

	// field initialization ---------------------------------------------------------
	mv.visitVarInsn(ALOAD, 0);
	mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
	mv.visitVarInsn(ALOAD, 0);
	mv.visitInsn(ACONST_NULL);
	mv.visitFieldInsn(PUTFIELD, invokableClassInternalName, "configReader", Type.getDescriptor(IDeploymentConfigurationReader.class));
	mv.visitVarInsn(ALOAD, 0);
	mv.visitInsn(ACONST_NULL);
	mv.visitFieldInsn(PUTFIELD, invokableClassInternalName, "invoker", Type.getDescriptor(IImplementationInvoker.class));

	// configReader = new ServletConfigurationReader(); ------------------------------
	mv.visitVarInsn(ALOAD, 0);
	mv.visitTypeInsn(NEW, Type.getInternalName(confReaderClass));
	mv.visitInsn(DUP);
	mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(confReaderClass), "<init>", "()V");
	mv.visitFieldInsn(PUTFIELD, invokableClassInternalName, "configReader", Type.getDescriptor(IDeploymentConfigurationReader.class));

	// invoker = new ImplementationInvoker();------
	mv.visitVarInsn(ALOAD, 0);
	mv.visitTypeInsn(NEW, Type.getInternalName(implmentationInvokerClass));
	mv.visitInsn(DUP);
	mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(implmentationInvokerClass), "<init>", "()V");
	mv.visitFieldInsn(PUTFIELD, invokableClassInternalName, "invoker", Type.getDescriptor(IImplementationInvoker.class));

	// invoker.initialize(configReader, String.class);
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, invokableClassInternalName, "invoker", Type.getDescriptor(IImplementationInvoker.class));
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, invokableClassInternalName, "configReader", Type.getDescriptor(IDeploymentConfigurationReader.class));
	mv.visitLdcInsn(wrappedClassName);
	mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IImplementationInvoker.class), "initialize", "("
		+ Type.getDescriptor(IDeploymentConfigurationReader.class)
		+ Type.getDescriptor(String.class) + ")V");

	mv.visitInsn(RETURN);
	Label l6 = new Label();
	mv.visitLabel(l6);

	mv.visitLocalVariable("this", invokableClassDescription, null, l0, l6, 0);
	mv.visitMaxs(4, 2);
	mv.visitEnd();
    }

    public void visitEnd() {
	// nothing to do  
    }

    public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
	// nothing to do  
    }

    public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3,
	    Object[] arg4) {
	// nothing to do  
    }

    public void visitIincInsn(int arg0, int arg1) {
	// nothing to do  
    }

    public void visitInsn(int arg0) {
	// nothing to do 
    }

    public void visitIntInsn(int arg0, int arg1) {
	// nothing to do   
    }

    public void visitJumpInsn(int arg0, Label arg1) {
	// nothing to do  
    }

    public void visitLabel(Label arg0) {
	// nothing to do   
    }

    public void visitLdcInsn(Object arg0) {
	// nothing to do    
    }

    public void visitLineNumber(int arg0, Label arg1) {
	// nothing to do   
    }

    public void visitLocalVariable(String arg0, String arg1, String arg2,
	    Label arg3, Label arg4, int arg5) {
	// nothing to do    
    }

    public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
	// nothing to do    
    }

    public void visitMaxs(int arg0, int arg1) {
	// nothing to do    
    }

    public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {
	// nothing to do    
    }

    public void visitMultiANewArrayInsn(String arg0, int arg1) {
	// nothing to do    
    }

    public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1,
	    boolean arg2) {
	return mv.visitParameterAnnotation(arg0, arg1, arg2);
    }

    public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
	    Label[] arg3) {
	// nothing to do    
    }

    public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2,
	    String arg3) {
	// nothing to do    
    }

    public void visitTypeInsn(int arg0, String arg1) {
	// nothing to do    
    }

    public void visitVarInsn(int arg0, int arg1) {
	// nothing to do
    }

}
