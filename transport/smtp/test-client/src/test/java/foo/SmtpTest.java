package foo;

import junit.framework.TestCase;
import javax.xml.ws.BindingProvider;
import org.jvnet.jax_ws_commons.transport.smtp.SMTPFeature;
import org.jvnet.jax_ws_commons.transport.smtp.POP3Info;
import org.jvnet.jax_ws_commons.transport.smtp.SenderInfo;
import com.sun.xml.ws.developer.WSBindingProvider;
import org.jvnet.jax_ws_commons.transport.smtp.client.SMTPTransportTube;

/**
 * @author Jitendra Kotamraju
 */
public class SmtpTest extends TestCase {
    /**
     * invokes using HTTP
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

    /**
     * invokes using SMTP
     */
    public void testSmtp() {
        SMTPTransportTube.dump = true;		// Enable logging

        SMTPFeature feature = new SMTPFeature("kohsuke.sfbay.sun.com", "10025",
            "smtp.transport.client@kohsuke.org");
        feature.setPOP3("kohsuke.org", "smtp.transport.client", "jaxws123");

        GreetingService proxy =
            new GreetingServiceService().getGreetingServicePort(feature);
        ((WSBindingProvider)proxy).setAddress("smtp://smtp.transport.server@kohsuke.org");

        assertEquals("Bonjour, jitu", proxy.sayHelloTo("jitu"));
    }
}
