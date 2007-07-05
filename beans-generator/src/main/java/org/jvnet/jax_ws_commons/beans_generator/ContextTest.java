/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ContextTest.java
 */

package org.jvnet.jax_ws_commons.beans_generator;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * Created: 15.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */

@WebService()
public class ContextTest {

    @WebMethod()
    public void method01() {
	System.out.println("invoking method 'method01'");
    }

    @WebMethod()
    public String methodHello(String name) {
	return "Hello, " + name + "!";
    }

    @WebMethod()
    public void methodALotOfArgs(String arg0, int arg1, long arg2, double arg3,
	    Integer arg4) {
	System.out.println("'" + arg0 + "'" + arg1 + "'" + arg2 + "'" + arg3
		+ "'" + arg4.toString() + "'");
    }
}
