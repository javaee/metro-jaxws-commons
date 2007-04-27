package foo;

import javax.jws.WebService;
import javax.jws.WebParam;
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

    public Book get(@WebParam(name="p1") int p1, @WebParam(name="p2") String p2) {
        return new Book(p1,p2);
    }

    public static final class Book {
        public int id = 1;
        public String title = "Java";
        public int p1;
        public String p2;
        public String nullValue = null;
        public float floatValue = 1.23456f;
        public double doubleValue = 1.23456;
        public boolean booleanValue = true;

        public Book() {
        }

        public Book(int p1, String p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

}
