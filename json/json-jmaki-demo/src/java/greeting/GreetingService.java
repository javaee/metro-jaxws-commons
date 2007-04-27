/*
 * GreetingService.java
 *
 * Created on April 19, 2007, 3:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package greeting;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

@WebService
public class GreetingService {
    
    @WebMethod
    public String sayHelloTo(@WebParam(name="name") String name) {
        return "Hello, "+name;
    }
}
