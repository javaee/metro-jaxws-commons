package grizzlytest;

import junit.framework.TestCase;

import javax.xml.ws.Endpoint;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jitendra Kotamraju
 */
public class Test extends TestCase {

    public void testApp() throws Exception {
        String address = "http://localhost:12345/echo";
        Endpoint endpoint = Endpoint.publish(address, new EchoService());
        hitEndpoint(new URL(address+"?wsdl"));
        endpoint.stop();
    }

    private void hitEndpoint(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.connect();
        dump(con);
    }

    private void dump(HttpURLConnection con) throws IOException {
        InputStream in = con.getErrorStream();
        if (in == null) {
            in = con.getInputStream();
        }
        int ch;
        while((ch=in.read()) != -1) {
            System.out.print((char)ch);
        }
        in.close();
    }

}
