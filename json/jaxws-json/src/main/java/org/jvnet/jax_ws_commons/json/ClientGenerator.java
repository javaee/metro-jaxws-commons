package org.jvnet.jax_ws_commons.json;

import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

import java.beans.Introspector;
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
        this.name = Introspector.decapitalize(model.getServiceQName().getLocalPart());
        if(name.endsWith("ServiceService"))
            // when doing java2wsdl and the class name ends with 'Service', you get this.
            name = name.substring(0,name.length()-7);
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

    private void writeGlobal(PrintWriter os) throws IOException {
        os.printf("%s = {\n",name);
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
        String reqName = jm.getRequestPayloadName().getLocalPart();
        String methodName = Introspector.decapitalize(jm.getOperationName());

        shift(os);
        os.printf("%s : function(obj, callback) {\n",methodName);
        shift2(os);
        os.printf("this.post({%s:obj},callback);\n", reqName);
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
