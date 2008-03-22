package org.jvnet.jax_ws_commons.transport.grizzly.server;


import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Implementation of a coyote adapter to process HTTP requests asynchronously
 *
 * This file uses the API in Glassfish / Grizzly 9.1
 *
 * The invocation pipeline when the <code>EmbeddedServerController</code> sets this
 * request processor looks as follows:
 *
 * -> Grizzly subsystem with ARP (asynchronous request processing) enabled
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
        LOGGER.fine("Received a request. The request thread "+Thread.currentThread()+" .");
        // TODO: synchornous execution for ?wsdl, non AsyncProvider requests
        WSHTTPConnection con = new JaxwsGrizzlyConnection(req, res, false); // TODO secure ??
        try {
            httpAdapter.invokeAsync(con);
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                res.setStatus(500);
                res.finishResponse();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
        LOGGER.fine("Getting out of service(). Done with the request thread "+Thread.currentThread()+" .");
    }

    public void afterService(GrizzlyRequest request, GrizzlyResponse response) throws Exception {
    }

}

