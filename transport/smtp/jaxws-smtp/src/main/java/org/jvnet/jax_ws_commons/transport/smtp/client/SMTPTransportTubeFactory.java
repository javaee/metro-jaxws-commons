package org.jvnet.jax_ws_commons.transport.smtp.client;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;

/**
 * SMTP Tranport Factory.
 *
 * @author Vivek Pandey
 */
public class SMTPTransportTubeFactory extends TransportTubeFactory {
    public Tube doCreate(@NotNull ClientTubeAssemblerContext context) {
        EndpointAddress address = context.getAddress();
        String scheme = address.getURI().getScheme();

        if (scheme != null && scheme.equalsIgnoreCase("smtp")) {
            return new SMTPTransportTube(context.getCodec(), context.getBinding(), address);
        }
        return null;
    }
}
