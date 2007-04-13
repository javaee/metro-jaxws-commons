package foo;

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import org.jvnet.jax_ws_commons.json.JSONBindingID;

/**
 * targetNamespace="http://jax-ws.dev.java.net/json" is a must.
 *
 * @author Jitendra Kotamraju
 */
@WebService(targetNamespace = "http://jax-ws.dev.java.net/json")
@BindingType(JSONBindingID.JSON_BINDING)
public class MyService {

    public Book get() {
        return new Book();
    }

    public static final class Book {
        public int id = 1;
        public String title = "Java";
    }

}
