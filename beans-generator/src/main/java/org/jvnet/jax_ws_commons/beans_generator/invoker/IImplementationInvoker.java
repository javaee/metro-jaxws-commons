/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: IImplementationInvoker.java
 */

package org.jvnet.jax_ws_commons.beans_generator.invoker;

import org.jvnet.jax_ws_commons.beans_generator.conf.IDeploymentConfigurationReader;

/**
 * Created: 16.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public interface IImplementationInvoker {
    /**
     * Initialize with configuration reader class instance and full name of 
     * implementator class (Class.getCanonicalName()).
     * 
     * @param configReader
     * @param implementationClass
     */
    public void initialize(IDeploymentConfigurationReader configReader,
	    String implementationClass);

    /**
     * Invoke implementator class.
     * 
     * @param methodName method's name
     * @param returnClass return class
     * @param argumentClasses argument classes
     * @param thrownTypes thrown types
     * @param returnValue return value (putted by mistake - contains null all time and didn't processed at all)
     * @param argumentValues argument values
     * @return return value
     */
    public Object invoke(String methodName, Class returnClass,
	    Class[] argumentClasses, Class[] thrownTypes, Object returnValue,
	    Object[] argumentValues) throws Exception;
}
