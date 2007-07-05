/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: IWSExceptionInfo.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador;

/**
 * Created: 08.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public interface IWSExceptionInfo {

    public String getOriginalClassName();
    
    /**
     * @return the className
     */
    public String getClassName();

    /**
     * @return the name
     */
    public String getName();

    /**
     * @return the ns
     */
    public String getNs();

    /**
     * @param className the className to set
     */
    public void setClassName(String className);

    /**
     * @param name the name to set
     */
    public void setName(String name);

    /**
     * @param ns the ns to set
     */
    public void setNs(String ns);

}
