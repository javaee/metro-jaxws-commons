package grizzlytest;

import junit.framework.TestCase;
import org.jvnet.jax_ws_commons.spring.SpringService;
import org.jvnet.jax_ws_commons.transport.grizzly.server.JaxwsGrizzlyTransport;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Response;
import javax.xml.ws.soap.SOAPBinding;
import java.util.Set;
import java.util.HashSet;

/**
 * Unit test for simple App.
 */
public class Test extends TestCase {

    public void testApp() throws Exception {

        EchoServer server = startServer();

        Service service = Service.create(new QName("FakeService"));

        String add = "http://localhost:8181/book";
        service.addPort(new QName("FakePort"), SOAPBinding.SOAP11HTTP_BINDING,add);
        JAXBContext jaxbCtx = JAXBContext.newInstance(Book.class);
        Dispatch dispatch = service.createDispatch(new QName("FakePort"),
            jaxbCtx, Service.Mode.PAYLOAD);

        Set<Response> respones = new HashSet<Response>();
        for(int i=0;i<10;i++) {
            Response r = dispatch.invokeAsync(
                    new Book("Midnight's Children", "Salman Rushdie", "Unknown"));
            assertNotNull(r);
            respones.add(r);
        }

        System.out.println("Going to sleep");
        Thread.sleep(3000);
        System.out.println("Let the hell break lose");
        server.respondToAll();

        for (Response respone : respones) {
            Book resp = (Book)respone.get();
            assertTrue(resp.getTitle().equals("Midnight's Children") &&
                resp.getPublisher().equals("Unknown") &&
                resp.getAuthor().equals("Salman Rushdie"));

        }

        System.out.println("Over");

    }

    private EchoServer startServer() throws Exception {
        // start a server
        JaxwsGrizzlyTransport server = new JaxwsGrizzlyTransport();
        server.setPort(8181);
        SpringService ss = new SpringService();
        EchoServer endpoint = new EchoServer();
        ss.setBean(endpoint);
        ss.afterPropertiesSet();
        server.setService(ss.getObject());
        server.afterPropertiesSet();

        return endpoint;
    }
}
