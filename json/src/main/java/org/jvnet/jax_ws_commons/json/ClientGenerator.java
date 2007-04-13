package org.jvnet.jax_ws_commons.json;

import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.JavaMethodImpl;

import javax.xml.namespace.QName;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

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
        writeInit(os);
        writePost(os);
        writePostFunc(os);
        writeOperations(os);
        os.close();
    }

    private void writeOperations(PrintStream os) {
        for(JavaMethodImpl op : model.getJavaMethods()) {
            writeOperation(op, os);
        }
    }

    // TODO: need to declare URL as global variable
    private void writeOperation(JavaMethodImpl jm, PrintStream os) {
        QName payload = model.getQNameForJM(jm);
        os.append("function ");
        os.append(jm.getMethod().getName());
        os.append("(url, obj, func) {\n");
        os.append("\treq = init();\n");
        os.append("\tvar wrapperObj = new Object();\n");
        os.append("\twrapperObj."+payload.getLocalPart()+" = obj;\n");
        os.append("\tpost(req, url, wrapperObj, func);\n");
        os.append("}\n");
        os.append("\n");
    }

    private void writeInit(PrintStream os) {
        os.append("function init() {\n");
        os.append("\tvar req;\n");
        os.append("\tif (window.XMLHttpRequest) {\n");
        os.append("\t\treq = new XMLHttpRequest();\n");
        os.append("\t} else if (window.ActiveXObject) {\n");
        os.append("\t\treq = new ActiveXObject(\"Microsoft.XMLHTTP\");\n");
        os.append("\t}\n");
        os.append("\treturn req;\n");
        os.append("}\n");
        os.append("\n");
    }

    private void writePost(PrintStream os) {
        os.append("function post(req, url, obj, func) {\n");
        os.append("\tif (req) {\n");
        os.append("\t\treq.onreadystatechange = postFunc(req,func);\n");
        os.append("\t\treq.open(\"POST\", url, true);\n");
        os.append("\t\treq.setRequestHeader(\"Content-Type\", \"application/json\");\n");
        os.append("\t\treq.send(obj);\n");
        os.append("\t}\n");
        os.append("}\n");
        os.append("\n");
    }

    private void writePostFunc(PrintStream os) {
        os.append("function postFunc(req, func) {\n");
        os.append("\tif (req.readyState == 4) {\n");
        os.append("\t\tif(req.status == 200) {\n");
        os.append("\t\t\tfunc(req.responseText);\n");
        os.append("\t\t} else {\n");
        os.append("\t\t\talert(\"Error:\"+req.status+\":\"+req.statusText);\n");
        os.append("\t\t}\n");
        os.append("\t}\n");
        os.append("}\n");
        os.append("\n");
    }
    
}
