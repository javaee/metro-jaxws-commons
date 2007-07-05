/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: ContextSubsystemException.java
 */

package org.jvnet.jax_ws_commons.beans_generator;


/**
 * Created: 04.06.2007
 * 
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class ContextSubsystemException extends Exception {

    /**
         * 
         */
    public ContextSubsystemException() {
	super();
    }

    /**
         * @param arg0
         * @param arg1
         */
    public ContextSubsystemException(String arg0, Throwable arg1) {
	super(arg0, arg1);
    }

    /**
         * @param arg0
         */
    public ContextSubsystemException(String arg0) {
	super(arg0);
    }

    /**
         * @param arg0
         */
    public ContextSubsystemException(Throwable arg0) {
	super(arg0);
    }

}
