package grizzlytest;

import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.WebServiceContext;

import com.sun.xml.ws.api.server.AsyncProvider;
import com.sun.xml.ws.api.server.AsyncProviderCallback;
import com.sun.xml.ws.api.server.AsyncProviderCallback;

/**
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
public class EchoServer implements AsyncProvider<Source> {

    public void invoke(Source request, AsyncProviderCallback<Source> callback,
        WebServiceContext ctxt) {

        System.out.println("Server invoked "+request);
        callback.send(request);
    }

}
