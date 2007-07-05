/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ContextStaticTest.java
 */

package org.jvnet.jax_ws_commons.beans_generator;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * Created: 20.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision$
 */
@WebService
public class ContextStaticTest {
    class ClassInnerClass {
	public void print() {
	    System.out.println("Hello from class inner class!");
	}
    }

    interface IClassInner {
	public void print();
    }

    private static boolean sf = false;

    static {
	System.out.println("Invoking static constructor!!!");
    }

    public ContextStaticTest() {
	if (!sf) {
	    sf = true;
	    System.out.println("Initializing sf!!!!");
	}
    }

    @WebMethod
    public void method() {
    }

    @WebMethod
    public void methodWithInner() {
	ClassInnerClass inner = new ClassInnerClass();
	inner.print();
    }

    @WebMethod
    public void methodMethodInner() {
	class MethodInnerClass {
	    public void print() {
		System.out.println("Hello from method inner class!");
	    }
	}
	MethodInnerClass inner = new MethodInnerClass();
	inner.print();
    }

    @WebMethod
    public void methodAnnonInner() {
	IClassInner inst = new IClassInner() {
	    public void print() {
		System.out.println("Hello from annonymous inner class!");
	    }
	};
	inst.print();

    }
    
    @WebMethod()
    public void thrower() throws Exception {
	throw new IllegalStateException("test exception");
    }
}
