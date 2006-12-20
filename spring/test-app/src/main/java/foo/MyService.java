package foo;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import java.util.Random;

/**
 * Canonical stock-quote web service.
 * @author Kohsuke Kawaguchi
 */
@WebService
@SOAPBinding(style=Style.RPC, use=Use.LITERAL)
public class MyService {
    Random r = new Random();
    public double quote(String ticker) {
        return r.nextDouble()*100;
    }
}
