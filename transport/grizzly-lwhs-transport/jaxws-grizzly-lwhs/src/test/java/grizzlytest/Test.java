package grizzlytest;

import junit.framework.TestCase;

import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.util.JAXBResult;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jitendra Kotamraju
 */
public class Test extends TestCase {

    public void testApp() throws Exception {
        String address = "http://localhost:12345/echo";
        Endpoint endpoint = Endpoint.publish(address, new EchoService());
        testWSDL(address);
        testService(address);
        endpoint.stop();
    }

    private void testService(String address) throws Exception {
        Service service = Service.create(new QName("FakeService"));
        service.addPort(new QName("FakePort"), SOAPBinding.SOAP11HTTP_BINDING, address);
        JAXBContext jaxbCtx = JAXBContext.newInstance(Book.class);
        Dispatch<Source> dispatch = service.createDispatch(new QName("FakePort"),
            Source.class, Service.Mode.PAYLOAD);

        String title = "Midnight's Children";
        String author = "Salman Rushdie";
        String publisher = "Unknown";
        Book book = new Book(title, author, publisher);

        JAXBElement<Book> elem = new JAXBElement<Book>(new QName("http://grizzlytest/", "echo"), Book.class, book);
        Source source = new JAXBSource(jaxbCtx, elem);
        source = dispatch.invoke(source);
        elem = jaxbCtx.createUnmarshaller().unmarshal(source, Book.class);
        assertEquals(new QName("http://grizzlytest/", "echoResponse"), elem.getName());
        
        book = elem.getValue();
        assertEquals(title, book.getTitle());
        assertEquals(publisher, book.getPublisher());
        assertEquals(author, book.getAuthor());
    }

    private void testWSDL(String address) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(address+"?wsdl").openConnection();
        con.connect();
        dump(con);
    }

    private void dump(HttpURLConnection con) throws IOException {
        assertEquals(200, con.getResponseCode());
        InputStream in = con.getErrorStream();
        if (in == null) {
            in = con.getInputStream();
        }
        int ch;
        while((ch=in.read()) != -1) {
            System.out.print((char)ch);
        }
        in.close();
    }

}
