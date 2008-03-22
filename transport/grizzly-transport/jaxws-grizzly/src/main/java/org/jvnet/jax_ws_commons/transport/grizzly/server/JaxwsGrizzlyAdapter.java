package org.jvnet.jax_ws_commons.transport.grizzly.server;


import java.util.logging.Logger;


import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.tcp.http11.*;
import com.sun.grizzly.http.AsyncTask;


import java.io.IOException;
import java.util.logging.Level;
import java.util.Map;

/**
 * Implementation of a coyote adapter to process HTTP requests asynchronously
 *
 * This file uses the API in Glassfish / Grizzly 9.1
 *
 * The invocation pipeline when the <code>EmbeddedServerController</code> sets this
 * request processor looks as follows:
 *
 * -> Grizzly subsystem with ARP (asynchronous request processing) enabled
 *     -> ** JaxwsGrizzlyAsyncFilter doFilter
 *         -> Grizzly ProcessorTask invokeAdapter
 *             -> ** JaxwsGrizzlyAdapter service (this is our Adapter implementation for Grizzly)
 *                 -> JAX-WS HttpAdapter invokeAsync (invokes the JAX-WS WSEndpoint asynchronously)
 *                     -> JAX-WS tube / pipeline plug-ins
 *
 *
 * The classes marked with ** are implemented by this module
 *
 * @author Jitendra Kotamraju
 */

public class JaxwsGrizzlyAdapter extends GrizzlyAdapter {

    private static final Logger LOGGER = Logger.getLogger(JaxwsGrizzlyAdapter.class.getName());

    /**
     * Index into the requests and response notes
     */
    final static int ADAPTER_NOTES = 1;

    /**
     * A mapping from the JBI message exchange ID to the request context
     */
    Map exchangeIDToContext = new java.util.concurrent.ConcurrentHashMap();

    private final WSEndpoint endpoint;

    private final HttpAdapter httpAdapter;

    public JaxwsGrizzlyAdapter(WSEndpoint endpoint, HttpAdapter httpAdapter) {
        this.endpoint = endpoint;
        this.httpAdapter = httpAdapter;
    }


    /**
     * Main entry point of the adapter to service a request
     * @param req incoming http request
     * @param res http response to prepare
     */
    public void service(GrizzlyRequest req, GrizzlyResponse res) {

        // Get the task associated with this request. This could be solved as a request note instead.
        AsyncTask asyncTask = JaxwsGrizzlyAsyncFilter.removeTaskMapping(req);

        LOGGER.log(Level.FINEST, "Got task mapping for request " + req.toString() + ", asyncProcessorTask " + asyncTask);


        // Prepare the request context
        Context currentContext = new Context();

        currentContext.req = req;
        currentContext.res = res;
        currentContext.asyncTask = asyncTask;

        // TODO: synchornous execution for ?wsdl, non AsyncProvider requests
        processAsynchRequest(currentContext);

    }

    public void afterService(GrizzlyRequest request, GrizzlyResponse response) throws Exception {
    }

    /**
     * @see Adapter
     */
    public void afterService(Request req, Response res) {
    }

    /**
     * Process a HttpRequest and send a JBI request.
     * @param reqContext embedded server request
     * @return JBI message exchange ID
     */
    public void processAsynchRequest(Context reqContext) {
        WSHTTPConnection con = new JaxwsGrizzlyConnection(reqContext.req, reqContext.res, reqContext.asyncTask, false);
        try {
            httpAdapter.invokeAsync(con);
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                reqContext.res.setStatus(500);
                reqContext.res.finishResponse();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Not supported by this adapter implementation.
     *
     * Notify all container event listeners that a particular event has
     * occurred for this Adapter.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireAdapterEvent(String type, Object data) {

    }

    /**
     * Holds request context information
     */
    public static class Context {
        GrizzlyRequest req;
        GrizzlyResponse res;
        String contextPath;
        String pathInfo;
        AsyncTask asyncTask;
    }

}

