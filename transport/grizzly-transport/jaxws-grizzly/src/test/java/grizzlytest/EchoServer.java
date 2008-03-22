package grizzlytest;

import com.sun.xml.ws.api.server.AsyncProvider;
import com.sun.xml.ws.api.server.AsyncProviderCallback;

import javax.xml.transform.Source;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

/**
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
public class EchoServer implements AsyncProvider<Source> {

    public void invoke(Source request, AsyncProviderCallback<Source> callback,
                       WebServiceContext ctxt) {

        System.out.println("Service is invoked " + request);
        new Thread(new RequestHandler(callback, request)).start();
    }

    private static class RequestHandler implements Runnable {
        final AsyncProviderCallback<Source> cbak;
        final Source req;

        public RequestHandler(AsyncProviderCallback<Source> cbak, Source req) {
            this.cbak = cbak;
            this.req = req;
        }

        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                cbak.sendError(new WebServiceException("Interrupted..."));
                return;
            }
            try {
                cbak.send(req);
            } catch (Exception e) {
                cbak.sendError(new WebServiceException(e));
            }
        }
    }

}