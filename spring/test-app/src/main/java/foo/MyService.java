package foo;

import javax.jws.WebService;
import java.util.Random;

/**
 * Canonical stock-quote web service.
 * @author Kohsuke Kawaguchi
 */
@WebService
public class MyService {
    Random r = new Random();
    public double quote(String ticker) {
        return r.nextDouble()*100;
    }
}
