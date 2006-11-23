package foo;

import static javax.jws.soap.SOAPBinding.ParameterStyle.BARE;
import static javax.jws.soap.SOAPBinding.Use.LITERAL;
import static javax.jws.soap.SOAPBinding.Style.RPC;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Dummy service class for testing
 * @author Kohsuke Kawaguchi
 */
@WebService
@SOAPBinding(style = RPC, use = LITERAL)
public class MyService {
    public String sayHelloTo(String n) {
        return "hello, "+n;
    }
}
