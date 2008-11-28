package org.jvnet.jax_ws_commons.guicemanaged;

import com.sun.xml.ws.api.server.InstanceResolverAnnotation;
import com.google.inject.Module;

import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;


/**
 * Define an annotation that marks a web service as GuiceManaged - meaning it will be possible to
 * inject members into that webservice.
 *
 * I.e. to use it in your project you would create your web service something like:
 *
 *
 <pre>
 &#064;GuiceManaged(module = WebServiceModule.class)
 &#064;WebService
 public class AddNumbersImpl implements AddNumbers {
   private Calculator calculator;
   &#064;Inject
   public void setCalculator(Calculator calc)
   {
      this.calculator=calc;
   }
   &#064;WebMethod
   public int addNumbers(&#064;WebParam(name="num1") int num1, &#064;WebParam(name="num2") int num2)
   {
      return this.calculator.calc(num1,num2);
   }
 }
 </pre>
 *
 * So, to use this feature, simply put these files on the classpath, annotate the web service with
 * the guice module you want to use and you are set.
 *
 * Note: it is not possible to use constructor-injection in the endpoint since endpoints in jax-ws must
 * have a default no-args constructor.
 *
 * @author Marcus Eriksson, krummas@gmail.com
 * @since Nov 4, 2008
 */

@Retention(RUNTIME)
@Target(TYPE)
@Documented
@WebServiceFeatureAnnotation(id=GuiceManagedFeature.ID, bean=GuiceManagedFeature.class)
@InstanceResolverAnnotation(GuiceManagedInstanceResolver.class)
public @interface GuiceManaged {
    public Class<? extends Module>[] module();
}
