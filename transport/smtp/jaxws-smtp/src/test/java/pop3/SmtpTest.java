package pop3;

import junit.framework.TestCase;
import org.jvnet.jax_ws_commons.transport.smtp.POP3Info;
import org.jvnet.jax_ws_commons.transport.smtp.SMTPFeature;
import org.jvnet.jax_ws_commons.transport.smtp.SenderInfo;
import org.jvnet.jax_ws_commons.transport.smtp.client.SmtpTransportTube;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.util.Properties;

/**
 * Unit test for simple App.
 */
public class SmtpTest extends TestCase {

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws JAXBException, ClassNotFoundException {
        Properties props = System.getProperties();
        props.put(SmtpTransportTube.class.getName()+".dump","true");

        SMTPFeature feature = new SMTPFeature();
        feature.setIncoming(new POP3Info("sun.com", "server", "password"));
        feature.setOutgoing(new SenderInfo("sun.com", null, "client@sun.com"));

        Service service = Service.create(new QName("FakeService"));

        String add = "smtp://server@sun.com";
        service.addPort(new QName("FakePort"), SOAPBinding.SOAP11HTTP_BINDING, add);
        JAXBContext jaxbCtx = JAXBContext.newInstance(Book.class);
        Dispatch dispatch = service.createDispatch(new QName("FakePort"), jaxbCtx, Service.Mode.PAYLOAD,
                feature);
        dispatch.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, add);
        Book resp = (Book) dispatch.invoke(new Book("Midnight's Children", "Salman Rushdie", "Unknown"));

        assertNotNull(resp);
        assertTrue(resp.getTitle().equals("Midnight's Children") &&
            resp.getPublisher().equals("Unknown") &&
            resp.getAuthor().equals("Salman Rushdie"));
    }
}
