package foo;

import junit.framework.TestCase;

import javax.xml.ws.Endpoint;
import java.util.Random;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class JsonTest extends TestCase {
    public void test1() throws Exception {
        // publish my service
        int port = new Random().nextInt(10000)+10000;
        String address = "http://localhost:" + port + "/book";
        Endpoint.publish(address, new MyService());

        MyClient.hitEndpoint(new URL(address));
    }
}
