package org.jvnet.jax_ws_commons.transport.asynchttpclient;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;

/**
 * @author Rama Pulavarthi
 */
public class AsyncHttpTransportTubeFactory extends TransportTubeFactory {
    @Override
    public Tube doCreate(@NotNull ClientTubeAssemblerContext context) {
        return new AsyncHttpTransportTube(context.getCodec(), context.getBinding());
    }
}
