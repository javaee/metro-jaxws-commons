package pop3;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.util.Properties;

import com.sun.mail.smtp.SMTPTransport;
import org.jvnet.jax_ws_commons.transport.smtp.client.SmtpTransportTube;

/**
 * Unit test for simple App.
 */
public class SmtpTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SmtpTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(SmtpTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws JAXBException, ClassNotFoundException {
        Properties props = System.getProperties();
        props.put(SmtpTransportTube.class.getName()+".dump","true");
        Service service = Service.create(new QName("FakeService"));
        service.getClass().getClassLoader().getResource("META-INF/services/com.sun.xml.ws.api.pipe.TransportTubeFactory");

        String add = "smtp://server@sun.com!pop3://server:password@sun.com/";
        service.addPort(new QName("FakePort"), SOAPBinding.SOAP11HTTP_BINDING, add);

        JAXBContext jaxbCtx = JAXBContext.newInstance(Book.class);
        Dispatch dispatch = service.createDispatch(new QName("FakePort"), jaxbCtx, Service.Mode.PAYLOAD);
        dispatch.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, add);
        Book resp = (Book) dispatch.invoke(new Book("Midnight's Children", "Salman Rushdie", "Unknown"));

        assertNotNull(resp);
        assertTrue(resp.getTitle().equals("Midnight's Children") &&
            resp.getPublisher().equals("Unknown") &&
            resp.getAuthor().equals("Salman Rushdie"));
    }
}
