/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.jvnet.jax_ws_commons.thread_scope;

import com.sun.xml.ws.api.server.InstanceResolverAnnotation;

import javax.jws.WebService;
import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Designates an endpoint class that should be tied to Thread scope.
 *
 * <p>
 * When a endpoint class is annotated with this annotation like the following,
 * the JAX-WS RI runtime will instanciate a new instance of the endpoint class
 * for each {@link Thread} and keeps the instance in a {@link ThreadLocal}
 * field.
 *
 * <pre>
 * &#64;{@link WebService}
 * &#64;{@link ThreadScope}
 * class DataService {
 *     Connection con;
 *
 *     public int delete() {
 *          Statement stmt = con.createStatement();
 *          stmt.executeQuery();
 *     }
 *
 *     public int add() {
 *          Statement stmt = con.createStatement();
 *          stmt.executeQuery();
 *     }
 * }
 * </pre>
 *
 * <p>
 * This allows you to use instance fields exclusively for an invocation and
 * to reuse exclusively for another invocation. Also service methods do not
 * have to be synchronized. (in the above example, same Connection object is
 * not used simultaneously for two requests.)
 *
 * <p>
 * The service instance will be GCed when the corresponding {@link Thread}
 * is GCed.
 *
 * @author Jitendra Kotamraju
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@WebServiceFeatureAnnotation(id=ThreadScopeFeature.ID, bean=ThreadScopeFeature.class)
@InstanceResolverAnnotation(ThreadScopeInstanceResolver.class)
public @interface ThreadScope {
}
