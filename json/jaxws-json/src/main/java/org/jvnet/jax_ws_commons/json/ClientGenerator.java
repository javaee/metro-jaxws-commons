package org.jvnet.jax_ws_commons.json;

import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.JavaMethodImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.util.Iterator;
import java.text.MessageFormat;

/**
 * Generates javascript stub code that is used to access the endpoint.
 * 
 * @author Jitendra Kotamraju
 */
final class ClientGenerator {
    private final AbstractSEIModelImpl model;

    ClientGenerator(AbstractSEIModelImpl model) {
        this.model = model;
    }

    void generate() throws IOException {
        PrintStream os = new PrintStream(new FileOutputStream("jaxws.js"));
        writeGlobal(os);
        writeStatic(os);
        writeOperations(os);
        writeClosure(os);
        os.close();
    }

    // TODO: need to declare URL as global variable
    private void writeGlobal(PrintStream os) throws IOException {
        String serviceName = model.getServiceQName().getLocalPart();
        os.append("var ");
        os.append(serviceName);
        os.append(" = {\n");
        shift(os);
        os.append("url : \"TODO\",\n");
    }

    private void writeStatic(PrintStream os) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("jaxws.js");
        int ch;
        while((ch = is.read()) != -1) {
            os.write(ch);
        }
        is.close();
    }

    private void writeOperations(PrintStream os) {
        Iterator<JavaMethodImpl> it = model.getJavaMethods().iterator();
        while(it.hasNext()) {
            writeOperation(it.next(), it.hasNext(), os);
        }
    }

    private void writeOperation(JavaMethodImpl jm, boolean next, PrintStream os) {
        String reqName = model.getQNameForJM(jm).getLocalPart();
        String methodName = jm.getMethod().getName();
        String resName = "getResponse"; // TODO

        shift(os);
        os.append(methodName);
        os.append(" : function(obj, callback) {\n");
        shift2(os);
        os.append("post({");
        os.append(reqName);
        os.append(" : obj}, function(obj) { callback(obj.");
        os.append(resName);
        os.append("); });\n");
        shift(os);
        if (next) { os.append("},\n\n"); } else { os.append("}\n\n"); }
    }

    private static void shift(PrintStream os) {
        os.append("    ");
    }

    private static void shift2(PrintStream os) {
        shift(os);
        shift(os);
    }

    private void writeClosure(PrintStream os) {
        os.append("};\n");
    }
    
}
