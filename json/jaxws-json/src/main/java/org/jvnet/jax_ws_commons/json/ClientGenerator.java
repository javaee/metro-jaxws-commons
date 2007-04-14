package org.jvnet.jax_ws_commons.json;

import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import com.sun.xml.ws.transport.http.HttpAdapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Iterator;

/**
 * Generates javascript stub code that is used to access the endpoint.
 * 
 * @author Jitendra Kotamraju
 */
final class ClientGenerator {
    private final SEIModel model;
    private final WSHTTPConnection connection;
    private final HttpAdapter adapter;
    private String name;

    public ClientGenerator(SEIModel model, WSHTTPConnection connection, HttpAdapter adapter) {
        this.model = model;
        this.connection = connection;
        this.adapter = adapter;
        this.name = model.getServiceQName().getLocalPart();
    }

    public void setVariableName(String name) {
        this.name = name;
    }

    void generate(PrintWriter os) throws IOException {
        writeGlobal(os);
        writeStatic(os);
        writeOperations(os);
        writeClosure(os);
        os.close();
    }

    // TODO: need to declare URL as global variable
    private void writeGlobal(PrintWriter os) throws IOException {
        os.printf("var %s = {\n",name);
        shift(os);
        os.printf("url : \"%s\",\n", connection.getBaseAddress()+adapter.urlPattern);
    }

    private void writeStatic(PrintWriter os) throws IOException {
        Reader is = new InputStreamReader(getClass().getResourceAsStream("jaxws.js"));
        char[] buf = new char[256];
        int len;
        while((len = is.read(buf)) != -1) {
            os.write(buf,0,len);
        }
        is.close();
    }

    private void writeOperations(PrintWriter os) {
        Iterator<? extends JavaMethod> it = model.getJavaMethods().iterator();
        while(it.hasNext()) {
            writeOperation(it.next(), it.hasNext(), os);
        }
    }

    private void writeOperation(JavaMethod jm, boolean next, PrintWriter os) {
        String reqName = jm.getPayloadName().getLocalPart();
        String methodName = jm.getMethod().getName();
        String resName = "getResponse"; // TODO

        shift(os);
        os.printf("%s : function(obj, callback) {\n",methodName);
        shift2(os);
        os.printf("post({%s:obj}, function(obj) { callback(obj.%s); });\n", reqName,resName);
        shift(os);
        if (next) { os.append("},\n\n"); } else { os.append("}\n\n"); }
    }

    private static void shift(PrintWriter os) {
        os.append("    ");
    }

    private static void shift2(PrintWriter os) {
        shift(os);
        shift(os);
    }

    private void writeClosure(PrintWriter os) {
        os.println("};");
    }
}
