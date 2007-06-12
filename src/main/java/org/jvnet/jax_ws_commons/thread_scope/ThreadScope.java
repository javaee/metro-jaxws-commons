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
