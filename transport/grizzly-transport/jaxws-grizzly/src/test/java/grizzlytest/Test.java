package grizzlytest;

import junit.framework.TestCase;
import org.jvnet.jax_ws_commons.spring.SpringService;
import org.jvnet.jax_ws_commons.transport.grizzly.server.GrizzlyTransport;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

/**
 * Unit test for simple App.
 */
public class Test extends TestCase {

    public void testApp() throws Exception {

        startServer();

        Service service = Service.create(new QName("FakeService"));

        String add = "http://localhost:8181/book";
        service.addPort(new QName("FakePort"), SOAPBinding.SOAP11HTTP_BINDING,
            add);
        JAXBContext jaxbCtx = JAXBContext.newInstance(Book.class);
        Dispatch dispatch = service.createDispatch(new QName("FakePort"),
            jaxbCtx, Service.Mode.PAYLOAD);

        Book resp = (Book) dispatch.invoke(
            new Book("Midnight's Children", "Salman Rushdie", "Unknown"));

        assertNotNull(resp);
        assertTrue(resp.getTitle().equals("Midnight's Children") &&
            resp.getPublisher().equals("Unknown") &&
            resp.getAuthor().equals("Salman Rushdie"));
    }

    private void startServer() throws Exception {
        // start a server
        GrizzlyTransport server = new GrizzlyTransport();
        server.setPort(8181);
        SpringService ss = new SpringService();
        ss.setBean(new EchoServer());
        ss.afterPropertiesSet();
        server.setService(ss.getObject());
        server.afterPropertiesSet();
    }
}
