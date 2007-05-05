package pop3;

import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;

/**
 * Echo back web service.
 * @author Kohsuke Kawaguchi
 */
@WebServiceProvider
public class EchoServer implements Provider<Source> {
    public Source invoke(Source request) {
        System.out.println("Server invoked "+request);
        return request;
    }
}
