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

    /**
     * This field illustrates how you can configure this bean from SPring.
     */
    private double base;

    public double getBase() {
        return base;
    }

    public void setBase(double base) {
        this.base = base;
    }

    public double quote(String ticker) {
        return r.nextDouble()*100;
    }
}
