package foo;

import javax.xml.ws.Endpoint;

/**
 * @author Jitendra Kotamraju
 */
public class MyDeployment {

    public static void main(String ... args) throws Exception {
        Endpoint.publish("http://localhost:1111/book", new MyService());
        Thread.sleep(0);
    }

}
