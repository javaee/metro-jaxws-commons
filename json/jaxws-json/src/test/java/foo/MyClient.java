package foo;

import java.io.*;
import java.net.*;

/**
 * @author Jitendra Kotamraju
 */
public class MyClient {

    public static void main(String ... args) throws Exception {
        URL url = new URL("http://localhost:1111/book");
        hitEndpoint(url);
    }

    static void hitEndpoint(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.addRequestProperty("Content-Type", "application/json");
        con.setDoInput(true);
        con.setDoOutput(true);
        // Write JSON request
        String json = "{get:{p1:50, p2:\"test\"} }";
        OutputStream out = con.getOutputStream();
        out.write(json.getBytes());
        out.close();

        // Check if we got the correct HTTP response code
        int code = con.getResponseCode();

        // Check if we got the correct response
        InputStream in = (code == 200) ? con.getInputStream() : con.getErrorStream();
        int ch;
        while((ch=in.read()) != -1) {
            System.out.print((char)ch);
        }
    }

}
