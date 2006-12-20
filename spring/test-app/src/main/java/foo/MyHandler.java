package foo;

import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.namespace.QName;
import java.util.Set;
import java.util.Collections;

/**
 * No-op dummy handler just to show that it can be configured.
 *
 * @author Kohsuke Kawaguchi
 */
public class MyHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    public boolean handleMessage(SOAPMessageContext context) {
        System.out.println("handling message");
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext context) {
    }
}
