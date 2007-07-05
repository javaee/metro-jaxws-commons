/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: IWSMethodInfo.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador;

/**
 * Created: 08.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 183 $
 */
public interface IWSMethodInfo {
    /**
     * Contains response bean name in case of Wrapped Document Mapping.
     * @return
     */
    String getResponseName();

    /**
     * Contains response bean namespace in case of Wrapped Document Mapping.
     * @return
     String*/
    String getResponseNS();

    /**
     * Contains response fully quallified bean class name in case of Wrapped Document Mapping.
     * @return
     */
    String getResponseBeanClassName();

    /**
     * Contains request bean name in case of Wrapped Document Mapping.
     * @return
     */
    String getRequestName();

    /**
     * Contains request bean namespace in case of Wrapped Document Mapping.
     * @return
     */
    String getRequestNS();

    /**
     * Contains request fully quallified bean class name in case of Wrapped Document Mapping.
     * @return
     */
    String getRequestBeanClassName();

    /**
     * Contains response bean name in case of Wrapped Document Mapping.
     * @return
     */
    void setResponseName(String value);

    /**
     * Contains response bean namespace in case of Wrapped Document Mapping.
     * @return
     */
    void setResponseNS(String value);

    /**
     * Contains response fully quallified bean class name in case of Wrapped Document Mapping.
     * @return
     */
    void setResponseBeanClassName(String value);

    /**
     * Contains request bean name in case of Wrapped Document Mapping.
     * @return
     */
    void setRequestName(String value);

    /**
     * Contains request bean namespace in case of Wrapped Document Mapping.
     * @return
     */
    void setRequestNS(String value);

    /**
     * Contains request fully quallified bean class name in case of Wrapped Document Mapping.
     * @return
     */
    void setRequestBeanClassName(String value);
}
