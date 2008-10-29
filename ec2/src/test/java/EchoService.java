import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;

/**
 * @author Kohsuke Kawaguchi
 */
@WebServiceProvider(wsdlLocation="echo.wsdl",serviceName="NewWebServiceService",targetNamespace = "http://test/", portName="NewWebServicePort")
public class EchoService implements Provider<Source> {
    public Source invoke(Source request) {
        return request;
    }
}
