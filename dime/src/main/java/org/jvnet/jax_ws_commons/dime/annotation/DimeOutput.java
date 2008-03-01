package org.jvnet.jax_ws_commons.dime.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.activation.DataHandler;

/**
 * Marker annotation for service endpoint methods that return binary attachments
 * in DIME encoded format. The method return type must be {@link DataHandler}.
 * 
 * @author Oliver Treichel
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DimeOutput {
    // Marker annotation
}
