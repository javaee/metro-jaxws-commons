/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: IEndpointData.java
 */

package org.jvnet.jax_ws_commons.beans_generator.conf;

import java.util.Collection;

/**
 * Container for information about ws endpoints.
 * 
 * Created: 04.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
public interface IEndpointData {
    /**
     * Inner class for configuration about real data.
     * 
     * Created: 04.07.2007
     * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
     * @version $Revision$
     */
    public static class EndpointConfiguration {
	private String wrappedClassName, invokableClassName, invokerClassName;

	/**
	 * FLagg to indicate: is there any need to wrap something or not?
	 */
	private boolean needToWrap = false;

	/**
	 * @return the needToWrap
	 */
	public boolean isNeedToWrap() {
	    return this.needToWrap;
	}

	/**
	 * @param wrappedClassName
	 * @param invokableClassName
	 * @param invokerClassName
	 */
	public EndpointConfiguration(String wrappedClassName,
		String invokableClassName, String invokerClassName,
		boolean needToWrap) {
	    super();
	    this.wrappedClassName = wrappedClassName;
	    this.invokableClassName = invokableClassName;
	    this.invokerClassName = invokerClassName;
	    this.needToWrap = needToWrap;
	}

	/**
	 * @return the invokableClassName
	 */
	public String getInvokableClassName() {
	    return this.invokableClassName;
	}

	/**
	 * @return the invokerClassName
	 */
	public String getInvokerClassName() {
	    return this.invokerClassName;
	}

	/**
	 * @return the wrappedClassName
	 */
	public String getWrappedClassName() {
	    return this.wrappedClassName;
	}

    }

    Collection<EndpointConfiguration> getEndpointPairs();

}
