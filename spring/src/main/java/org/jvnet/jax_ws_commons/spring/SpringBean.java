package org.jvnet.jax_ws_commons.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Represents beans that are created by Spring for configuration.
 *
 * <p>
 * This marker is purely for documentation purpose.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(SOURCE)
@Documented
public @interface SpringBean {
}
