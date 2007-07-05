/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ASMUtil.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador.impl.asm;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckAnnotationAdapter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.CheckFieldAdapter;
import org.objectweb.asm.util.CheckMethodAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * Created: 06.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class ASMUtil {
    public static int getVarSize(int typeSort) {
	if (typeSort == Type.ARRAY) return 1;
	if (typeSort == Type.BOOLEAN) return 1;
	if (typeSort == Type.BYTE) return 1;
	if (typeSort == Type.CHAR) return 1;
	if (typeSort == Type.DOUBLE) return 2;
	if (typeSort == Type.FLOAT) return 1;
	if (typeSort == Type.INT) return 1;
	if (typeSort == Type.LONG) return 2;
	if (typeSort == Type.OBJECT) return 1;
	if (typeSort == Type.SHORT) return 1;
	if (typeSort == Type.VOID) return 1;
	return 1;
    }

    public static int getVarSize(Type type) {
	return getVarSize(type.getSort());
    }

    public static int calculateLocalVarSize(Type[] argTypes) {
	int result = 1; // don't forget about 'this'
	for (Type type : argTypes)
	    result += getVarSize(type);
	return result;
    }

    public static boolean isPrimitiveType(Type type) {
	if (type.equals(Type.BOOLEAN_TYPE) || type.equals(Type.BYTE_TYPE)
		|| type.equals(Type.CHAR_TYPE) || type.equals(Type.DOUBLE_TYPE)
		|| type.equals(Type.FLOAT_TYPE) || type.equals(Type.INT_TYPE)
		|| type.equals(Type.LONG_TYPE) || type.equals(Type.SHORT_TYPE)
		|| type.equals(Type.VOID_TYPE)) return true;
	return false;
    }

    public static Type getWrapperForPrimitiveType(Type primitiveType) {
	if (primitiveType.equals(Type.BOOLEAN_TYPE)) {
	    return Type.getType(Boolean.class);
	}
	if (primitiveType.equals(Type.BYTE_TYPE)) {
	    return Type.getType(Byte.class);
	}
	if (primitiveType.equals(Type.CHAR_TYPE)) {
	    return Type.getType(Character.class);
	}
	if (primitiveType.equals(Type.DOUBLE_TYPE)) {
	    return Type.getType(Double.class);
	}
	if (primitiveType.equals(Type.FLOAT_TYPE)) {
	    return Type.getType(Float.class);
	}
	if (primitiveType.equals(Type.INT_TYPE)) {
	    return Type.getType(Integer.class);
	}
	if (primitiveType.equals(Type.LONG_TYPE)) {
	    return Type.getType(Long.class);
	}
	if (primitiveType.equals(Type.SHORT_TYPE)) {
	    return Type.getType(Short.class);
	}
	if (primitiveType.equals(Type.VOID_TYPE)) {
	    return Type.getType(Void.class);
	}
	throw new IllegalStateException("Unknown primitive type "
		+ primitiveType.toString());
    }

    public static ClassVisitor createCheckedClassWriter() {
	ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
	CheckClassAdapter cv = new CheckClassAdapter(writer);
	return cv;
    }

    public static ClassWriter createClassWriter() {
	return new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    }

    public static AnnotationVisitor wrapIntoCheckedVisitor(
	    AnnotationVisitor visitor) {
	return new CheckAnnotationAdapter(visitor);
    }

    public static ClassVisitor wrapIntoCheckedVisitor(ClassVisitor visitor) {
	return new CheckClassAdapter(visitor);
    }

    public static ClassVisitor wrapIntoTracer(ClassVisitor cv) {
	PrintWriter ps = new PrintWriter(System.out);
	TraceClassVisitor result = new TraceClassVisitor(cv, ps);
	return result;
    }

    public static MethodVisitor wrapIntoCheckedVisitor(MethodVisitor visitor) {
	return new CheckMethodAdapter(visitor);
    }

    public static FieldVisitor wrapIntoCheckedVisitor(FieldVisitor visitor) {
	return new CheckFieldAdapter(visitor);
    }

    public static ClassReader createClassReader(final String className)
	    throws IOException {
	InputStream is =
		Thread.currentThread().getContextClassLoader().getResourceAsStream(className.replace(".", "/")
			+ ".class");
	return new ClassReader(is);
    }
}
