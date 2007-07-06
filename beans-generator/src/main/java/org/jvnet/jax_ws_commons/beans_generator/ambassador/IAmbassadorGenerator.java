/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: IAmbassadorGenerator.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador;

import java.lang.reflect.Method;

import org.jvnet.jax_ws_commons.beans_generator.conf.IEndpointData;
import org.jvnet.jax_ws_commons.beans_generator.invoker.IImplementationInvoker;


/**
 * Created: 04.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public interface IAmbassadorGenerator {
    void generateAndLoadClasses(String wrappedClassName,
	    String invocableClassName, WSImplemetatorAnnotationInfo implInfo,
	    Class<? extends IImplementationInvoker> implementationInvokerClass,
	    Class confReaderClass, IEndpointData endpointData)
	    throws AmbassadorGenerationException;

    public IWSMethodInfo constructMethodInfo(Method md);

    public IWSExceptionInfo constructExceptionInfo(Class cd);

    /**
     * Get class loader with all necessary loaded classes.
     * 
     * Recieved class loader will serve as proxy for all common classs loaders and 
     * primary class loader for generated classes. 
     * 
     * 
     * @return
     * 
     */
    public ClassLoader getAmbassadorClassLoader(ClassLoader originalCL);
}
