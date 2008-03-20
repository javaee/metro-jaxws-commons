package foo;

import junit.framework.TestCase;

import com.sun.xml.ws.developer.WSBindingProvider;

/**
 * @author Jitendra Kotamraju
 */
public class GrizzlyTest extends TestCase {
    /**
     * invokes using HTTP
     */
    public void testHttp() {
        GreetingService proxy =
            new GreetingServiceService().getGreetingServicePort();
        ((WSBindingProvider)proxy).setAddress("http://localhost:8080/soap");
        assertEquals("Bonjour, jitu", proxy.sayHelloTo("jitu"));
    }

    /**
     * invokes using grizzly
     */
    public void testGrizzly() throws Exception {

        GreetingService proxy =
            new GreetingServiceService().getGreetingServicePort();
        ((WSBindingProvider)proxy).setAddress("http://localhost:8181/soap");

        assertEquals("Bonjour, jitu", proxy.sayHelloTo("jitu"));
        ((WSBindingProvider)proxy).close();
    }
}
