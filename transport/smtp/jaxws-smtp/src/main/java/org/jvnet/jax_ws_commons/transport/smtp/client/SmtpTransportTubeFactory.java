package org.jvnet.jax_ws_commons.transport.smtp.client;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;

import javax.xml.ws.WebServiceException;

/**
 * SMTP Tranport Factory.
 *
 * @author Vivek Pandey
 */
public class SmtpTransportTubeFactory extends TransportTubeFactory {
    public Tube doCreate(@NotNull ClientTubeAssemblerContext context) {
         EndpointAddress address = context.getAddress();
        String scheme = address.getURI().getScheme();
        if (scheme != null) {
            if(scheme.equalsIgnoreCase("smtp"))
                return new SmtpTransportTube(context.getCodec(), address);
        }

        throw new WebServiceException("Unsupported endpoint address: "+context.getAddress());
    }
}
