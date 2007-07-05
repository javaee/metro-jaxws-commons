/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: IAmbassadorGeneratorClassLoader.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador;

/**
 * Base 'ambassador' class loader .
 * 
 * Redirects all requests for classes to delegatee (original) class loader, except 
 * requests for generated class loader.
 * 
 * Created: 21.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision$
 */
public abstract class AmbassadorGeneratorClassLoaderAbstract extends ClassLoader {
    abstract public void setDelegateeClassLoader(ClassLoader classLoader);

    abstract public ClassLoader getDelegateeClassLoader();
}
