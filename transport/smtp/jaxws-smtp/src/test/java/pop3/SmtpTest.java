package pop3;

import junit.framework.TestCase;
import org.jvnet.jax_ws_commons.spring.SpringService;
import org.jvnet.jax_ws_commons.transport.smtp.POP3Info;
import org.jvnet.jax_ws_commons.transport.smtp.SMTPFeature;
import org.jvnet.jax_ws_commons.transport.smtp.SenderInfo;
import org.jvnet.jax_ws_commons.transport.smtp.client.SMTPTransportTube;
import org.jvnet.jax_ws_commons.transport.smtp.server.ServerSMTPFeature;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

/**
 * Unit test for simple App.
 */
public class SmtpTest extends TestCase {

    public void testSenderOnly() throws Exception {
        SMTPTransportTube.dump = true;

        SMTPFeature feature = new SMTPFeature("sun.com", null, "client@sun.com", true);
        Service service = Service.create(new QName("FakeService"));

        String add = "smtp://server@sun.com";
        service.addPort(new QName("FakePort"), SOAPBinding.SOAP11HTTP_BINDING, add);
        JAXBContext jaxbCtx = JAXBContext.newInstance(Book.class);
        Dispatch dispatch = service.createDispatch(new QName("FakePort"), jaxbCtx, Service.Mode.PAYLOAD,
                feature);
        dispatch.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, add);
        dispatch.invokeOneWay(new Book("Midnight's Children", "Salman Rushdie", "Unknown"));
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws Exception {
        SMTPTransportTube.dump = true;

        startServer();

        SMTPFeature feature = new SMTPFeature("sun.com","client@sun.com");
        feature.setPOP3("sun.com", "client", "password");

        Service service = Service.create(new QName("FakeService"));

        String add = "smtp://server@sun.com";
        service.addPort(new QName("FakePort"), SOAPBinding.SOAP11HTTP_BINDING, add);
        JAXBContext jaxbCtx = JAXBContext.newInstance(Book.class);
        Dispatch dispatch = service.createDispatch(new QName("FakePort"), jaxbCtx, Service.Mode.PAYLOAD,
                feature);
        dispatch.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, add);
        Book resp = (Book) dispatch.invoke(new Book("čáp - Midnight's Children", "Salman Rushdie", "Unknown"));

        assertNotNull(resp);
        assertTrue(resp.getTitle().equals("čáp - Midnight's Children") &&
            resp.getPublisher().equals("Unknown") &&
            resp.getAuthor().equals("Salman Rushdie"));
    }

    private void startServer() throws Exception {
        // start a server
        ServerSMTPFeature server = new ServerSMTPFeature();
        server.setIncoming(new POP3Info("sun.com","server","password"));
        server.setOutgoing(new SenderInfo("sun.com","server@sun.com"));
        SpringService ss = new SpringService();
        ss.setBean(new EchoServer());
        ss.afterPropertiesSet();
        server.setService(ss.getObject());
        server.afterPropertiesSet();
    }
}
