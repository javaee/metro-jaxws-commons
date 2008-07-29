/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.jvnet.jax_ws_commons.spring;

import org.springframework.beans.factory.FactoryBean;

import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.MTOM;

/**
 * Configures MTOM feature.
 *
 * @org.apache.xbean.XBean element="mtom"
 * @author Kohsuke Kawaguchi
 */
// this kind of wrapping is unnecessary if we can perform XBean processing on the feature bean
// itself. But MTOMFeature is a spec feature and we can't do anything about it, so this is a wrapper
public class SpringMTOMFeature implements FactoryBean {

    private boolean enabled = true;
    private int threshold;

    private MTOMFeature feature;

    public boolean isSingleton() {
        return true;
    }

    /**
     * <tt>enabled="false"</tt> can be specified to override the {@link MTOM} annotation
     * on the source code. Defaults to true.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The size in bytes that binary data SHOULD be before
     * being sent as an attachment.
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public MTOMFeature getObject() throws Exception {
        if(feature==null)
            feature = new MTOMFeature(enabled,threshold);
        return feature;
    }

    public Class getObjectType() {
        return MTOMFeature.class;
    }
}
