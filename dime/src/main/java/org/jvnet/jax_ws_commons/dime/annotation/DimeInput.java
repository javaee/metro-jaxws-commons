package org.jvnet.jax_ws_commons.dime.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.activation.DataHandler;

/**
 * Marker annotation for service endpoint methods that expect binary attachments
 * in DIME encoded format. At least one method parameter must be of type
 * {@link DataHandler}.
 * 
 * @author Oliver Treichel
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DimeInput {
    // Marker annotation
}
