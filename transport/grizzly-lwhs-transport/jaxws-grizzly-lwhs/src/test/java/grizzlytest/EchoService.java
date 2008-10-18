package grizzlytest;

import javax.jws.WebService;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;

/**
 * @author Jitendra Kotamraju
 */
@WebService
@SOAPBinding(parameterStyle=SOAPBinding.ParameterStyle.BARE)
public class EchoService {

    public Book echo(Book book) {
        return book;
    }

}