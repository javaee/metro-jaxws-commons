/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ContextUtils.java
 */

package org.jvnet.jax_ws_commons.beans_generator;


/**
 * Created: 08.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class ContextUtils {
    public static String getAnnValue(String value, String defaultValue) {
	if (null != value) {
	    if (!value.equals("")) return value;
	    else return defaultValue;
	} else return defaultValue;

    }

    public static Object createDefaultReturnValueByClass(Class clazz) {
	if (clazz.equals(Integer.TYPE)) return new Integer(0);
	if (clazz.equals(Byte.TYPE)) return new Byte((byte) 0);
	if (clazz.equals(Character.TYPE)) return new Character(' ');
	if (clazz.equals(Float.TYPE)) return new Float(0);
	if (clazz.equals(Double.TYPE)) return new Double(0);
	if (clazz.equals(Long.TYPE)) return new Long(0);
	return null;
    }
    
}
