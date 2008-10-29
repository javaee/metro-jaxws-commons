package com.sun.xml.ws.commons.ec2;

import com.sun.xml.ws.api.pipe.TransportPipeFactory;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.transport.http.client.HttpTransportPipe;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.EndpointAddress;

import java.net.URI;

/**
 * @author Kohsuke Kawaguchi
  */
 public class TransportTubeFactoryImpl extends TransportTubeFactory {
    public Tube doCreate(@NotNull ClientTubeAssemblerContext context) {
        URI uri = context.getAddress().getURI();
        String scheme = uri.getScheme();
        if (scheme.equals("ec2")) {
            final EndpointAddress address = new EndpointAddress(URI.create("https" + uri.toString().substring(3)));

            return new HttpTransportPipe(context.getCodec(), context.getBinding()) {
                public Packet process(Packet request) {
                    request.endpointAddress = address;
                    Packet rsp = super.process(request);
                    rsp.wasTransportSecure = false;
                    return rsp;
                }
            };
        }
        return null;
    }
}
