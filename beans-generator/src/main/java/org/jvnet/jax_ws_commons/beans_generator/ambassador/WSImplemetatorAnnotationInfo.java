/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: WSImplemetatorAnnotationInfo.java
 */

package org.jvnet.jax_ws_commons.beans_generator.ambassador;

import java.util.Collection;

/**
 * Created: 08.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision: 240 $
 */
@SuppressWarnings("unused")
public class WSImplemetatorAnnotationInfo {
    public enum MappingType {
	DOC_WRAPPED, DOC_BARE, RPC;
    }

    private String serviceName = null;

    private String name = null;

    private String targetNS = null;

    private Collection<IWSMethodInfo> methodInfos;

    private Collection<IWSExceptionInfo> exceptionInfos;

    private MappingType mappingType;

    public MappingType getMappingType() {
	return this.mappingType;
    }

    public void setMappingType(MappingType mappingType) {
	this.mappingType = mappingType;
    }

    public Collection<IWSMethodInfo> getMethodInfos() {
	return this.methodInfos;
    }

    public void setMethodInfos(Collection<IWSMethodInfo> methodMetadatas) {
	this.methodInfos = methodMetadatas;
    }

    public Collection<IWSExceptionInfo> getExceptionInfos() {
	return this.exceptionInfos;
    }

    public void setExceptionInfos(Collection<IWSExceptionInfo> exceptionInfos) {
	this.exceptionInfos = exceptionInfos;
    }

    public String getTargetNS() {
	return this.targetNS;
    }

    public void setTargetNS(String targetNS) {
	this.targetNS = targetNS;
    }

    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getServiceName() {
	return this.serviceName;
    }

    public void setServiceName(String serviceName) {
	this.serviceName = serviceName;
    }
}
