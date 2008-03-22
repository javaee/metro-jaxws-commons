package grizzlytest;

import com.sun.xml.ws.api.server.AsyncProvider;
import com.sun.xml.ws.api.server.AsyncProviderCallback;

import javax.xml.transform.Source;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
public class EchoServer implements AsyncProvider<Source> {

    private final Set<RequestHandler> pendingRequests = new HashSet<RequestHandler>();

    public synchronized void invoke(Source request, AsyncProviderCallback<Source> callback,
                       WebServiceContext ctxt) {

        System.out.println("Service is invoked " + request);
        pendingRequests.add(new RequestHandler(callback, request));
    }

    public synchronized void respondToAll() {
        for (RequestHandler request : pendingRequests)
            request.run();
    }

    private static class RequestHandler implements Runnable {
        final AsyncProviderCallback<Source> cbak;
        final Source req;

        public RequestHandler(AsyncProviderCallback<Source> cbak, Source req) {
            this.cbak = cbak;
            this.req = req;
        }

        public void run() {
            cbak.send(req);
        }
    }
}