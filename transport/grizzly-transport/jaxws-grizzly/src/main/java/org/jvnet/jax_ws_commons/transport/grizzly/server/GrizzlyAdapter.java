package org.jvnet.jax_ws_commons.transport.grizzly.server;

import org.apache.coyote.Request;
import org.apache.coyote.Response;

import java.util.logging.Logger;

/**
 * @author Jitendra Kotamraju
 */
public class GrizzlyAdapter extends com.sun.enterprise.web.connector.grizzly.standalone.StaticResourcesAdapter {

    private static final Logger LOGGER = Logger.getLogger(GrizzlyAdapter.class.getName());

    @Override
    public void service(Request req, final Response res) throws Exception {
        LOGGER.info("Received request="+req);
    }

}
