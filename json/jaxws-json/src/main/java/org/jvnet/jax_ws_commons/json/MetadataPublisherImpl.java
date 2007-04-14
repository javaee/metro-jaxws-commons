package org.jvnet.jax_ws_commons.json;

import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.api.model.SEIModel;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Responds to "http://foobar/service?js" and sends the JavaScript proxy.
 *
 * @author Kohsuke Kawaguchi
 */
public class MetadataPublisherImpl extends HttpMetadataPublisher {
    private SEIModel model;

    public MetadataPublisherImpl(SEIModel model) {
        this.model = model;
    }

    @Override
    public boolean handleMetadataRequest(@NotNull WSHTTPConnection con) throws IOException {
        if(!con.getQueryString().equals("js"))
            return false;

        con.setStatus(HttpURLConnection.HTTP_OK);
        con.setContentTypeResponseHeader("application/javascript;charset=utf-8");

        new ClientGenerator(model).generate(new PrintWriter(
            new OutputStreamWriter(con.getOutput(),"UTF-8")));

        return true;
    }
}
