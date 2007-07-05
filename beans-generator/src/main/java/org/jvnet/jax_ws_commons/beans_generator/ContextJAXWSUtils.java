/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ContextJAXWSUtils.java
 */

package org.jvnet.jax_ws_commons.beans_generator;

import java.lang.reflect.Method;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Created: 08.06.2007
 * 
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class ContextJAXWSUtils {
    public static String mapJavaNameToXMLName(String name) {
	return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String mapXMLNameToJavaName(String name) {
	return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static String generateRequestWrapperClassName(Method md,
	    Class containigClass) {
	String pack = containigClass.getName().substring(0,
		containigClass.getName().lastIndexOf("."));
	return pack + "." + ContextJAXWSConstants.JAXWS_SUBPACKAGE_NAME + "."
		+ mapJavaNameToXMLName(md.getName());

    }

    public static String generateRequestWrapperName(Method md) {
	return mapJavaNameToXMLName(md.getName());

    }

    public static String generateResponseWrapperClassName(Method md,
	    Class containigClass) {
	String pack = containigClass.getName().substring(0,
		containigClass.getName().lastIndexOf("."));
	return pack + "." + ContextJAXWSConstants.JAXWS_SUBPACKAGE_NAME + "."
		+ mapJavaNameToXMLName(md.getName()) + "Response";
    }

    public static String generateResponseWrapperName(Method md) {
	return mapJavaNameToXMLName(md.getName()) + "Response";
    }

    public static String generateExceptionWrapperClassName(Class cd,
	    Class methodHolderClass) {
	String pack = methodHolderClass.getName().substring(0,
		methodHolderClass.getName().lastIndexOf("."));
	return pack + "." + ContextJAXWSConstants.JAXWS_SUBPACKAGE_NAME + "."
		+ cd.getSimpleName() + "Bean";
    }

    public static String generateTargetNSFromClass(Class cd) {
	Package p = cd.getPackage();
	String packageName = p.getName();
	StringTokenizer st = new StringTokenizer(packageName, ".");
	StringBuffer sb = new StringBuffer("http://");
	Stack<String> q = new Stack<String>();
	while (st.hasMoreTokens())
	    q.push(st.nextToken());
	while (q.size() > 0)
	    sb.append(q.pop() + (q.size() > 0 ? "." : ""));
	sb.append("/");
	return sb.toString();

    }

}
