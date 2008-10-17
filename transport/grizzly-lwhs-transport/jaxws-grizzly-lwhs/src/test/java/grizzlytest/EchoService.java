package grizzlytest;

import javax.jws.WebService;

/**
 * @author Jitendra Kotamraju
 */
@WebService
public class EchoService {

    public Book echo(Book book) {
        return book;
    }

}