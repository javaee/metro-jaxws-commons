package foo;

import junit.framework.TestCase;

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
        assertEquals("Bonjour, jitu", proxy.sayHelloTo("jitu"));
    }

    public void testSmtp() {
    }
}
