package org.jvnet.jax_ws_commons.transport.smtp.server;

import org.jvnet.jax_ws_commons.transport.smtp.SMTPFeature;

import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.*;

/**
 * To enable endpoint processing requests using SMTP transport, use
 * this annotation.
 * 
 * @author Jitendra Kotamraju
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebServiceFeatureAnnotation(id= SMTPFeature.ID,bean=SMTPFeature.class)
public @interface SMTP {
}
