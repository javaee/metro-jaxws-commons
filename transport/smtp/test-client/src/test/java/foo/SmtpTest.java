package foo;

import junit.framework.TestCase;
import java.util.Properties;
import javax.xml.ws.BindingProvider;
import org.jvnet.jax_ws_commons.transport.smtp.client.SmtpTransportTube;
import org.jvnet.jax_ws_commons.transport.smtp.SMTPFeature;
import org.jvnet.jax_ws_commons.transport.smtp.POP3Info;
import org.jvnet.jax_ws_commons.transport.smtp.SenderInfo;

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

        SMTPFeature feature = new SMTPFeature();
        feature.setIncoming(new POP3Info("kohsuke.org", "smtp.transport.client",
            "jaxws123"));
        feature.setOutgoing(new SenderInfo("kohsuke.sfbay.sun.com", "10025",
            "smtp.transport.client@kohsuke.org"));
        GreetingService proxy =
            new GreetingServiceService().getGreetingServicePort(feature);

        ((BindingProvider)proxy).getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            "smtp://smtp.transport.server@kohsuke.org"
        );
        assertEquals("Bonjour, jitu", proxy.sayHelloTo("jitu"));
    }
}
