package grizzlytest;

import junit.framework.TestCase;

import javax.xml.ws.Endpoint;

/**
 * @author Jitendra Kotamraju
 */
public class Test extends TestCase {

    public void testApp() throws Exception {
        Endpoint endpoint = Endpoint.publish("http://localhost:12345/echo", new EchoService());
        endpoint.stop();
    }

}
