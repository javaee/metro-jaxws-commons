/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: JAXWSInvalidAnnotationException.java
 */

package org.jvnet.jax_ws_commons.beans_generator;

/**
 * Created: 08.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 183 $
 */
public class JAXWSInvalidAnnotationException extends ContextSubsystemException {

    /**
     * 
     */
    public JAXWSInvalidAnnotationException() {
	super();
    }

    /**
     * @param arg0
     * @param arg1
     */
    public JAXWSInvalidAnnotationException(String arg0, Throwable arg1) {
	super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public JAXWSInvalidAnnotationException(String arg0) {
	super(arg0);
    }

    /**
     * @param arg0
     */
    public JAXWSInvalidAnnotationException(Throwable arg0) {
	super(arg0);
    }

}
