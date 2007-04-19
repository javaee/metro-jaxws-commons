package greeter;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import org.jvnet.jax_ws_commons.transport.smtp.server.SMTP;

@WebService
@SMTP
public class GreetingService {
    private String prefix = "Hello, ";

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @WebMethod
    public String sayHelloTo(@WebParam(name="name") String name) {
        return prefix+name;
    }
}
