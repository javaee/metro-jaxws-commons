/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ASMFaultBeanGenerator.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador.impl.asm;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jax_ws_commons.beans_generator.ContextJAXWSUtils;
import org.jvnet.jax_ws_commons.beans_generator.ambassador.IWSExceptionInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


/**
 * Created: 02.07.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision$
 */
public class ASMFaultBeanGenerator {
    private String[] excludeGetters =
	    { "getCause", "getLocalizedMessage", "getStackTrace", "getClass" };

    class ASMFaultBeanGeneratorInner implements ClassVisitor {

	private ClassWriter cv = null;

	private Map<String, String> attributes =
		new LinkedHashMap<String, String>();

	public ASMFaultBeanGeneratorInner(ClassWriter cv) {
	    this.cv = cv;
	}

	public void visit(int version, int access, String name,
		String signature, String superName, String[] interfaces) {
	    cv.visit(Opcodes.V1_5, access, exceptionInfo.getClassName().replace(".", "/"), null, Type.getInternalName(Object.class), null);
	}

	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
	    return null;
	}

	public void visitAttribute(Attribute arg0) {
	    return;
	}

	public FieldVisitor visitField(int arg0, String arg1, String arg2,
		String arg3, Object arg4) {
	    return null;
	}

	public void visitInnerClass(String arg0, String arg1, String arg2,
		int arg3) {
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
		String signature, String[] exceptions) {

	    //if (name.equals("<init>")) return cv.visitMethod(access, name, desc, signature, exceptions);

	    for (String exclude : excludeGetters)
		if (exclude.equals(name)) return null;

	    // collect all getters
	    if (name.startsWith("get")) {
		String attributeName =
			name.substring(3, 4).toLowerCase() + name.substring(4);
		Type returnType = Type.getReturnType(desc);
		attributes.put(attributeName, returnType.getDescriptor());
	    }
	    return null;
	}

	public void visitOuterClass(String arg0, String arg1, String arg2) {

	}

	public void visitSource(String arg0, String arg1) {

	}

	public void visitEnd() {
	    String cin =
		    "L" + exceptionInfo.getClassName().replace(".", "/") + ";";
	    // create setter and getters for all cathed getters
	    for (String key : attributes.keySet()) {
		String desc = attributes.get(key);
		cv.visitField(Opcodes.ACC_PRIVATE, key, desc, null, null);
		createGetter(cv, key, desc, cin);
		createSetter(cv, key, desc, cin);
	    }
	    // create necessary annotations
	    generateAnnotations();
	    generateNoArgsConstructor();
	    cv.visitEnd();
	}

	private void createSetter(ClassVisitor cv, String propertyName,
		String type, String classInternalName) {
	    String methodName =
		    "set" + propertyName.substring(0, 1).toUpperCase()
			    + propertyName.substring(1);
	    MethodVisitor mv =
		    cv.visitMethod(Opcodes.ACC_PUBLIC, methodName, "(" + type
			    + ")V", null, null);
	    mv.visitVarInsn(Opcodes.ALOAD, 0);
	    mv.visitVarInsn(Type.getType(classInternalName).getOpcode(Opcodes.ILOAD), 1);
	    mv.visitFieldInsn(Opcodes.PUTFIELD, classInternalName, propertyName, type);
	    mv.visitInsn(Opcodes.RETURN);
	    mv.visitMaxs(0, 0);
	}

	private void createGetter(ClassVisitor cv, String propertyName,
		String returnType, String classInternalName) {
	    String methodName =
		    "get" + propertyName.substring(0, 1).toUpperCase()
			    + propertyName.substring(1);
	    MethodVisitor mv =
		    cv.visitMethod(Opcodes.ACC_PUBLIC, methodName, "()"
			    + returnType, null, null);
	    mv.visitVarInsn(Opcodes.ALOAD, 0);
	    mv.visitFieldInsn(Opcodes.GETFIELD, classInternalName, propertyName, returnType);
	    mv.visitInsn(Type.getType(classInternalName).getOpcode(Opcodes.IRETURN));
	    mv.visitMaxs(0, 0);
	}

	private void generateAnnotations() {
	    AnnotationVisitor av =
		    cv.visitAnnotation(Type.getDescriptor(XmlRootElement.class), true);

	    av.visit("name", ContextJAXWSUtils.mapXMLNameToJavaName(exceptionInfo.getName()));
	    av.visit("namespace", exceptionInfo.getNs());
	    av.visitEnd();

	    av = cv.visitAnnotation(Type.getDescriptor(XmlType.class), true);
	    av.visit("name", ContextJAXWSUtils.mapXMLNameToJavaName(exceptionInfo.getName()));
	    av.visit("namespace", exceptionInfo.getNs());
	    av.visitEnd();

	    av =
		    cv.visitAnnotation(Type.getDescriptor(XmlAccessorType.class), true);
	    av.visitEnum("value", Type.getDescriptor(XmlAccessType.class), XmlAccessType.FIELD.toString());
	    av.visitEnd();
	}
	
	  private void generateNoArgsConstructor() {
		MethodVisitor mv =
			writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	    }
    }

    private ClassWriter writer = null;

    private IWSExceptionInfo exceptionInfo = null;

    public byte[] generate() {

	try {
	    ASMFaultBeanGeneratorInner bg =
		    new ASMFaultBeanGeneratorInner(writer);
	    ClassReader cr =
		    ASMUtil.createClassReader(exceptionInfo.getOriginalClassName());
	    cr.accept(bg, ClassReader.SKIP_CODE & ClassReader.SKIP_DEBUG
		    & ClassReader.SKIP_FRAMES);
	    return writer.toByteArray();
	} catch (IOException e) {
	    log.severe("Exception while generating exception class: "
		    + e.getMessage());
	    return null;
	}
    }

    private Logger log = null;

    /**
     * @param writer
     * @param exceptionInfo
     */
    public ASMFaultBeanGenerator(ClassWriter writer,
	    IWSExceptionInfo exceptionInfo) {
	super();
	this.writer = writer;
	this.exceptionInfo = exceptionInfo;
	log = Logger.getLogger(this.getClass().getCanonicalName());
    }

    public ASMFaultBeanGenerator(IWSExceptionInfo exceptionInfo) {
	this(ASMUtil.createClassWriter(), exceptionInfo);
    }
}
