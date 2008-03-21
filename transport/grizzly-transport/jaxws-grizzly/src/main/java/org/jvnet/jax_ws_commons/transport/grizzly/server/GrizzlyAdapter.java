package org.jvnet.jax_ws_commons.transport.grizzly.server;

import org.apache.coyote.Request;
import org.apache.coyote.Response;

import java.util.logging.Logger;



import com.sun.enterprise.web.connector.grizzly.AsyncTask;
import com.sun.enterprise.web.connector.grizzly.ByteBufferStream;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

import org.apache.coyote.Adapter;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.coyote.http11.InternalInputBuffer;
import org.apache.coyote.http11.InternalOutputBuffer;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.Map;

import javax.security.auth.Subject;

/**
 * Implementation of a coyote adapter to process HTTP requests asynchronously
 *
 * This file uses the API in Glassfish / Grizzly 9.1
 *
 * The invocation pipeline when the <code>EmbeddedServerController</code> sets this
 * request processor looks as follows:
 *
 * -> Grizzly subsystem with ARP (asynchronous request processing) enabled
 *     -> ** GrizzlyAsyncFilter doFilter
 *         -> Grizzly ProcessorTask invokeAdapter
 *             -> ** GrizzlyAdapter service (this is our Adapter implementation for Grizzly)
 *                 -> JAX-WS HttpAdapter invokeAsync (invokes the JAX-WS WSEndpoint asynchronously)
 *                     -> JAX-WS tube / pipeline plug-ins
 *
 *
 * The classes marked with ** are implemented by this module
 *
 * @author Jitendra Kotamraju
 */

public class GrizzlyAdapter implements Adapter {

    private static final Logger LOGGER = Logger.getLogger(GrizzlyAdapter.class.getName());

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

    public GrizzlyAdapter(WSEndpoint endpoint, HttpAdapter httpAdapter) {
        this.endpoint = endpoint;
        this.httpAdapter = httpAdapter;
    }


    /**
     * Main entry point of the adapter to service a request
     * @param req incoming http request
     * @param res http response to prepare
     */
    public void service(Request req, Response res) {

        // Get the task associated with this request. This could be solved as a request note instead.
        AsyncTask asyncTask = GrizzlyAsyncFilter.removeTaskMapping(req);

        LOGGER.log(Level.FINEST, "Got task mapping for request " + req.toString() + ", asyncProcessorTask " + asyncTask);


        LOGGER.log(Level.FINE, "Service async request for: " + req.requestURI());

        // Prepare the request context
        Context currentContext = new Context();
        currentContext.anInputBuffer = (InternalInputBuffer) req.getInputBuffer();
        currentContext.anOutputBuffer = (InternalOutputBuffer) res.getOutputBuffer();
        currentContext.req = req;
        currentContext.res = res;
        currentContext.asyncTask = asyncTask;

        // TODO: beware, request parsing does not always seem intuitive
        //currentContext.contextPath = req.localName().toString();
        currentContext.contextPath = "";
        currentContext.pathInfo = req.requestURI().toString();

        // TODO: synchornous execution for ?wsdl, non AsyncProvider requests
        processAsynchRequest(currentContext);

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
        WSHTTPConnection con = new GrizzlyConnection(reqContext.req, reqContext.res, reqContext.asyncTask, false);
        try {
            httpAdapter.invokeAsync(con);
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                reqContext.res.setStatus(500);
                reqContext.res.finish();
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
        InternalInputBuffer anInputBuffer;
        InternalOutputBuffer anOutputBuffer;
        Request req;
        Response res;
        String contextPath;
        String pathInfo;
        AsyncTask asyncTask;
    }

}

