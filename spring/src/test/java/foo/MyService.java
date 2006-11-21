package foo;

import javax.jws.WebService;

/**
 * Dummy service class for testing
 * @author Kohsuke Kawaguchi
 */
@WebService
public class MyService {
    public String sayHelloTo(String n) {
        return "hello, "+n;
    }
}
