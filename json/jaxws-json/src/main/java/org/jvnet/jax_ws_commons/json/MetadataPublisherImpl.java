package org.jvnet.jax_ws_commons.json;

import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.transport.http.HttpAdapter;
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
final class MetadataPublisherImpl extends HttpMetadataPublisher {
    private SEIModel model;

    public MetadataPublisherImpl(SEIModel model) {
        this.model = model;
    }

    @Override
    public boolean handleMetadataRequest(@NotNull HttpAdapter adapter, @NotNull WSHTTPConnection con) throws IOException {
        QueryStringParser qsp = new QueryStringParser(con);
        if(!qsp.containsKey("js"))
            return false;

        con.setStatus(HttpURLConnection.HTTP_OK);
        con.setContentTypeResponseHeader("application/javascript;charset=utf-8");

        ClientGenerator gen = new ClientGenerator(model, con, adapter);
        String varName = qsp.get("var");
        if(varName!=null)
            gen.setVariableName(varName);

        gen.generate(new PrintWriter(
            new OutputStreamWriter(con.getOutput(),"UTF-8")));

        return true;
    }
}
