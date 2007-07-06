/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ASMAmbassadorWrapperMethodGenerator.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador.impl.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jvnet.jax_ws_commons.beans_generator.ambassador.impl.asm.ASMAmbassadorGenerator.MethodInfo;
import org.jvnet.jax_ws_commons.beans_generator.invoker.IImplementationInvoker;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


/**
 * Generator of method wrappers.
 * 
 * Created: 06.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class ASMAmbassadorWrapperMethodGenerator implements Opcodes {

    public final static int VARIABLE_COUNT = 5;

    private ClassVisitor codeWriter = null;

    private String invocableClassName = null;

    /**
     * @param codeWriter
     */
    public ASMAmbassadorWrapperMethodGenerator(ClassVisitor codeWriter,
	    String invocableClassName) {
	super();
	this.codeWriter = codeWriter;
	this.invocableClassName = invocableClassName;
    }

    /**
     * @param methodData
     */
    public void generateMethod(MethodInfo methodData) {
	// ------------------------------------------------------------------
	List<Type> originalTypes =
		Arrays.asList(Type.getArgumentTypes(methodData.getDesc()));
	List<Type> newArgTypes = new ArrayList<Type>(originalTypes);
    // i don't want add this functionality  into productive version
	// change method arguments to accept additional one - String with context name 
	// newArgTypes.add(Type.getType(String.class));
	Type returnType = Type.getReturnType(methodData.getDesc());
	

	String desc =
		Type.getMethodDescriptor(returnType, newArgTypes.toArray(new Type[0]));
	methodData.setDesc(desc);

	// create methods 
	MethodVisitor mv =
		codeWriter.visitMethod(methodData.getAccess(), methodData.getName(), methodData.getDesc(), methodData.getSignature(), methodData.getExceptions());
	// wrap for reliability
	mv = ASMUtil.wrapIntoCheckedVisitor(mv);

	// create necessary annotations
	createAnnotations(mv, methodData);

	// starting label (for local variable visibility ranges)
	Label startLabel = new Label();

	createPrologue(mv, methodData, startLabel);
	createInvokationArgs(mv, methodData);
	createInvokation(mv, methodData);
	createReturn(mv, methodData);
	createEpilogue(mv, methodData, startLabel);

    }

    /**
     * @param mv
     */
    private void createAnnotations(MethodVisitor mv, MethodInfo methodInfo) {
	AnnotationVisitor av =
		mv.visitAnnotation(Type.getDescriptor(javax.jws.WebMethod.class), true);
	av.visit("operationName", methodInfo.getName());
	av.visitEnd();

    }

    /**
     * @param mv
     * @param methodData
     * @param startLabel
     */
    private void createPrologue(MethodVisitor mv, MethodInfo methodData,
	    Label startLabel) {
	// start writing code
	mv.visitCode();
	mv.visitLabel(startLabel);
    }

    /**
     * @param mv
     * @param methodData
     */
    private void createInvokationArgs(MethodVisitor mv, MethodInfo methodData) {
	int localVarsSize =
		ASMUtil.calculateLocalVarSize(methodData.getArgTypes());

	// set return class type
	Type returnType = methodData.getReturnType();
	putTypeClassIntoStack(mv, returnType);
	mv.visitVarInsn(ASTORE, localVarsSize++);

	// store argument classes into array
	mv.visitIntInsn(BIPUSH, methodData.getArgTypes().length);
	mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(Class.class));

	for (int counter = 0; counter < methodData.getArgTypes().length; counter++) {
	    mv.visitInsn(DUP);
	    mv.visitIntInsn(BIPUSH, counter);
	    putTypeClassIntoStack(mv, methodData.getArgTypes()[counter]);
	    mv.visitInsn(AASTORE);
	}
	mv.visitVarInsn(ASTORE, localVarsSize++);

	// set exception classes
	if (null != methodData.getExceptions()) {
	    mv.visitIntInsn(BIPUSH, methodData.getExceptions().length);
	    mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(Class.class));
	    for (int counter = 0; counter < methodData.getExceptions().length; counter++) {
		mv.visitInsn(DUP);
		mv.visitIntInsn(BIPUSH, counter);
		mv.visitLdcInsn(Type.getType("L"
			+ methodData.getExceptions()[counter] + ";"));
		mv.visitInsn(AASTORE);
	    }
	} else {
	    mv.visitInsn(ICONST_0);
	    mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(Class.class));
	}
	mv.visitVarInsn(ASTORE, localVarsSize++);

	// set return type to null;
	mv.visitInsn(ACONST_NULL);
	mv.visitVarInsn(ASTORE, localVarsSize++);

	// store all arguments into object array
	mv.visitIntInsn(BIPUSH, methodData.getArgTypes().length);
	mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));

	int variableCounter = 1;
	for (int counter = 0; counter < methodData.getArgTypes().length; counter++) {
	    mv.visitInsn(DUP);
	    mv.visitIntInsn(BIPUSH, counter);
	    variableCounter =
		    takeArgFromVariablesConvertAndPush(mv, counter, methodData.getArgTypes(), variableCounter);
	    mv.visitInsn(AASTORE);
	}

	mv.visitVarInsn(ASTORE, localVarsSize++);

    }

    /**
     * @param mv
     * @param methodData
     */
    private void createInvokation(MethodVisitor mv, MethodInfo methodData) {
	String genInternalClassName = invocableClassName.replace(".", "/");
	int localVarsSize =
		ASMUtil.calculateLocalVarSize(methodData.getArgTypes());
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, genInternalClassName, "invoker", Type.getDescriptor(IImplementationInvoker.class));
	mv.visitLdcInsn(methodData.getName()); // method name
	mv.visitVarInsn(ALOAD, localVarsSize); // returnType
	mv.visitVarInsn(ALOAD, localVarsSize + 1); // argumentClasses
	mv.visitVarInsn(ALOAD, localVarsSize + 2); //  thrownTypes
	mv.visitVarInsn(ALOAD, localVarsSize + 3); //  return Value
	mv.visitVarInsn(ALOAD, localVarsSize + 4); // argumentValues
	mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IImplementationInvoker.class), "invoke", "(Ljava/lang/String;Ljava/lang/Class;[Ljava/lang/Class;[Ljava/lang/Class;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");

	if (methodData.getReturnType().getSort() != Type.VOID) mv.visitVarInsn(ASTORE, localVarsSize + 3);
	else mv.visitInsn(POP);

    }

    /**
     * @param mv
     * @param methodData
     */
    private void createReturn(MethodVisitor mv, MethodInfo methodData) {
	Type returnType = methodData.getReturnType();
	int localVarsSize =
		ASMUtil.calculateLocalVarSize(methodData.getArgTypes());
	int returnValVarPosition = localVarsSize + 3;
	int castedResultVarPosition = localVarsSize + 5;
	boolean isReturnSomething =
		(methodData.getReturnType().getSort() != Type.VOID);

	switch (returnType.getSort()) {
	case Type.ARRAY: {
	    mv.visitVarInsn(ALOAD, returnValVarPosition);
	    mv.visitTypeInsn(CHECKCAST, returnType.getInternalName());

	    mv.visitInsn(ARETURN);
	    break;
	}
	case Type.BOOLEAN: {
	    mv.visitVarInsn(ALOAD, returnValVarPosition);
	    mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
	    if (isReturnSomething) mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
	    mv.visitInsn(IRETURN);
	    break;
	}
	case Type.BYTE: {
	    mv.visitVarInsn(ALOAD, returnValVarPosition);
	    mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
	    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
	    mv.visitInsn(ARETURN);

	    break;
	}
	case Type.CHAR: {
	    mv.visitVarInsn(ALOAD, returnValVarPosition);
	    mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
	    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
	    mv.visitInsn(IRETURN);

	    break;
	}
	case Type.DOUBLE: {
	    mv.visitVarInsn(ALOAD, returnValVarPosition);
	    mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
	    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
	    mv.visitInsn(DRETURN);
	    break;
	}
	case Type.FLOAT: {
	    mv.visitVarInsn(ALOAD, returnValVarPosition);
	    mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
	    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
	    mv.visitInsn(FRETURN);
	    break;
	}
	case Type.INT: {
	    mv.visitVarInsn(ALOAD, returnValVarPosition);
	    mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
	    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
	    mv.visitInsn(IRETURN);
	    break;
	}
	case Type.LONG: {
	    mv.visitVarInsn(ALOAD, returnValVarPosition);
	    mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
	    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
	    mv.visitInsn(LRETURN);
	    break;
	}
	case Type.OBJECT: {
	    mv.visitVarInsn(ALOAD, returnValVarPosition);
	    mv.visitTypeInsn(CHECKCAST, returnType.getInternalName());
	    mv.visitInsn(ARETURN);
	    break;
	}
	case Type.SHORT: {
	    mv.visitVarInsn(ALOAD, returnValVarPosition);
	    mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
	    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
	    mv.visitInsn(IRETURN);
	    break;
	}
	case Type.VOID: {
	    mv.visitInsn(RETURN);
	    break;
	}
	default: {
	    mv.visitInsn(RETURN);
	    break;
	}
	}
    }

    /**
     * @param mv
     * @param methodData
     */
    private void createEpilogue(MethodVisitor mv, MethodInfo methodData,
	    Label startLabel) {

	/*
	 Label finishLabel = new Label();
	 mv.visitLabel(finishLabel);
	 String genInternalClassName = invokableClassName.replace(".", "/");
	 mv.visitLocalVariable("this", "L" + genInternalClassName + ";", null, startLabel, finishLabel, 0);
	 mv.visitLocalVariable("returnType", "Ljava/lang/Class;", null, startLabel, finishLabel, 1);
	 mv.visitLocalVariable("argumentClasses", "[Ljava/lang/Class;", null, startLabel, finishLabel, 2);
	 mv.visitLocalVariable("thrownTypes", "[Ljava/lang/Class;", null, startLabel, finishLabel, 3);
	 mv.visitLocalVariable("returnValue", "Ljava/lang/Object;", null, startLabel, finishLabel, 4);
	 mv.visitLocalVariable("argumentValues", "[Ljava/lang/Object;", null, startLabel, finishLabel, 5);
	 */
	int localVarsSize =
		ASMUtil.calculateLocalVarSize(methodData.getArgTypes());
	mv.visitMaxs(localVarsSize + VARIABLE_COUNT + 1, VARIABLE_COUNT
		+ localVarsSize);
	mv.visitEnd();
    }

    /**
     * Put type's class into stack, according to its type.
     * 
     * @param mv
     * @param type
     */
    private void putTypeClassIntoStack(MethodVisitor mv, Type type) {

	if (ASMUtil.isPrimitiveType(type)) {
	    mv.visitFieldInsn(GETSTATIC, ASMUtil.getWrapperForPrimitiveType(type).getInternalName(), "TYPE", Type.getDescriptor(Class.class));
	} else mv.visitLdcInsn(type);
    }

    /**
     * @param counter
     * @param argTypes
     */
    private int takeArgFromVariablesConvertAndPush(MethodVisitor mv,
	    int counter, Type[] argTypes, int variableCounter) {
	if (argTypes[counter].getSort() == Type.OBJECT) {
	    mv.visitVarInsn(ALOAD, variableCounter);
	    variableCounter += ASMUtil.getVarSize(argTypes[counter]);
	    return variableCounter;
	    // int
	} else if (argTypes[counter].getSort() == Type.INT) {
	    mv.visitVarInsn(ILOAD, variableCounter);
	    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
	    variableCounter += ASMUtil.getVarSize(argTypes[counter]);
	    return variableCounter;
	    // []
	} else if (argTypes[counter].getSort() == Type.ARRAY) {
	    mv.visitVarInsn(ALOAD, variableCounter);
	    variableCounter += ASMUtil.getVarSize(argTypes[counter]);
	    return variableCounter;
	    // boolean
	} else if (argTypes[counter].getSort() == Type.BOOLEAN) {
	    mv.visitVarInsn(ILOAD, variableCounter);
	    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
	    variableCounter += ASMUtil.getVarSize(argTypes[counter]);
	    return variableCounter;
	    // byte
	} else if (argTypes[counter].getSort() == Type.BYTE) {
	    mv.visitVarInsn(ILOAD, variableCounter);
	    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
	    variableCounter += ASMUtil.getVarSize(argTypes[counter]);
	    return variableCounter;
	    // char
	} else if (argTypes[counter].getSort() == Type.CHAR) {
	    mv.visitVarInsn(ILOAD, variableCounter);
	    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
	    variableCounter += ASMUtil.getVarSize(argTypes[counter]);
	    return variableCounter;
	    // double
	} else if (argTypes[counter].getSort() == Type.DOUBLE) {
	    mv.visitVarInsn(DLOAD, variableCounter);
	    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
	    variableCounter += ASMUtil.getVarSize(argTypes[counter]);
	    return variableCounter;
	    // float
	} else if (argTypes[counter].getSort() == Type.FLOAT) {
	    mv.visitVarInsn(FLOAD, variableCounter);
	    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
	    variableCounter += ASMUtil.getVarSize(argTypes[counter]);
	    return variableCounter;
	} else if (argTypes[counter].getSort() == Type.LONG) {
	    mv.visitVarInsn(LLOAD, variableCounter);
	    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
	    variableCounter += ASMUtil.getVarSize(argTypes[counter]);
	    return variableCounter;
	} else if (argTypes[counter].getSort() == Type.SHORT) {
	    mv.visitVarInsn(ILOAD, variableCounter);
	    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
	    variableCounter += ASMUtil.getVarSize(argTypes[counter]);
	    return variableCounter;
	}

	throw new IllegalStateException("Unknown type "
		+ argTypes[counter].toString());
    }
}
