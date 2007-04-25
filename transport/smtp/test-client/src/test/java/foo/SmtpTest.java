package foo;

import junit.framework.TestCase;
import java.util.Properties;
import javax.xml.ws.BindingProvider;
import org.jvnet.jax_ws_commons.transport.smtp.client.SmtpTransportTube;

/**
 * @author Jitendra Kotamraju
 */
public class SmtpTest extends TestCase {
    /**
     * 
     */
    public void testHttp() {
        GreetingService proxy =
            new GreetingServiceService().getGreetingServicePort();
        ((BindingProvider)proxy).getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            "http://localhost:8080/soap"
        );
        assertEquals("Bonjour, jitu", proxy.sayHelloTo("jitu"));
    }

    public void testSmtp() {
        Properties props = System.getProperties();
        props.put("mail.smtp.host","kohsuke.sfbay.sun.com");
        props.put("mail.smtp.port","10025");
        props.put(SmtpTransportTube.class.getName()+".dump","true");

        GreetingService proxy =
            new GreetingServiceService().getGreetingServicePort();
        ((BindingProvider)proxy).getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            "smtp://smtp.transport.server@kohsuke.org?!pop3://smtp.transport.client:jaxws123@kohsuke.org/"
        );
        assertEquals("Bonjour, jitu", proxy.sayHelloTo("jitu"));
    }
}
