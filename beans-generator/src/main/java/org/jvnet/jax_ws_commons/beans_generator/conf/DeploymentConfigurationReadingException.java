/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: DeploymentConfigurationReadingException.java
 */

package org.jvnet.jax_ws_commons.beans_generator.conf;

import org.jvnet.jax_ws_commons.beans_generator.ContextSubsystemException;

/**
 * Created: 04.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public class DeploymentConfigurationReadingException extends
	ContextSubsystemException {

    /**
     * 
     */
    public DeploymentConfigurationReadingException() {
	super();
    }

    /**
     * @param arg0
     * @param arg1
     */
    public DeploymentConfigurationReadingException(String arg0, Throwable arg1) {
	super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public DeploymentConfigurationReadingException(String arg0) {
	super(arg0);
    }

    /**
     * @param arg0
     */
    public DeploymentConfigurationReadingException(Throwable arg0) {
	super(arg0);
    }

}
