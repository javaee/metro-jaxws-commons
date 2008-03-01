package org.sample.ws.dime;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.BindingType;

import org.jvnet.jax_ws_commons.dime.annotation.DimeInput;
import org.jvnet.jax_ws_commons.dime.annotation.DimeOutput;
import org.jvnet.jax_ws_commons.dime.binding.DimeBindingID;

@WebService
@SOAPBinding(style = Style.RPC)
@BindingType(DimeBindingID.DIME_BINDING)
public class Dime {
    @WebMethod
    @DimeOutput
    public DataHandler getFile() {
        final URL url = getClass().getResource("javaxml-duke.gif");
        final DataHandler data = new DataHandler(url);
        return data;
    }

    @WebMethod
    @DimeInput
    public void setFile(@WebParam(name = "data")
    final DataHandler data) {
        try {
            data.writeTo(new FileOutputStream("/tmp/dummy.gif"));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
