package foo;

import javax.jws.WebService;
import javax.jws.WebMethod;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

/**
 * @author Kohsuke Kawaguchi
 */
@WebService
public class MyService {
    private Random random = new Random();
    private int range;

    public void setRange(int r) {
        this.range = r*100;
    }

    @WebMethod
    public BigDecimal quote(String tickerSymbol) {
        return new BigDecimal(new BigInteger(String.valueOf(random.nextInt(range))),2);
    }
}
