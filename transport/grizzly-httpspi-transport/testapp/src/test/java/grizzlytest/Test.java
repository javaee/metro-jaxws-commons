/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package grizzlytest;

import junit.framework.TestCase;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.jvnet.jax_ws_commons.transport.grizzly_httpspi.GrizzlyHttpContextFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.spi.http.HttpContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Jitendra Kotamraju
 */
public class Test extends TestCase {

    public void testApp() throws Exception {
        String contextPath = "/ctxt";
        String path = "/echo";
        int port = 12345;

        String address = "http://localhost:"+port+contextPath+path;

        HttpServer server = new HttpServer();
        NetworkListener listener = new NetworkListener("test", NetworkListener.DEFAULT_NETWORK_HOST, port);
        server.addListener(listener);
        HttpContext context = GrizzlyHttpContextFactory.createHttpContext(server, contextPath, path);

        Endpoint endpoint = Endpoint.create(new EchoService());
        endpoint.publish(context);

        server.start();

        testWSDL(address);
        testService(address);
        endpoint.stop();

        server.stop();
    }

    private void testService(String address) throws Exception {
        Service service = Service.create(new QName("FakeService"));
        service.addPort(new QName("FakePort"), SOAPBinding.SOAP11HTTP_BINDING, address);
        JAXBContext jaxbCtx = JAXBContext.newInstance(Book.class);
        Dispatch<Source> dispatch = service.createDispatch(new QName("FakePort"),
            Source.class, Service.Mode.PAYLOAD);

        String title = "Midnight's Children";
        String author = "Salman Rushdie";
        String publisher = "Unknown";
        Book book = new Book(title, author, publisher);

        JAXBElement<Book> elem = new JAXBElement<Book>(new QName("http://grizzlytest/", "echo"), Book.class, book);
        Source source = new JAXBSource(jaxbCtx, elem);
        source = dispatch.invoke(source);
        elem = jaxbCtx.createUnmarshaller().unmarshal(source, Book.class);
        assertEquals(new QName("http://grizzlytest/", "echoResponse"), elem.getName());
        
        book = elem.getValue();
        assertEquals(title, book.getTitle());
        assertEquals(publisher, book.getPublisher());
        assertEquals(author, book.getAuthor());
    }

    private void testWSDL(String address) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(address+"?wsdl").openConnection();
        con.connect();
        dump(con);
    }

    private void dump(HttpURLConnection con) throws IOException {
        assertEquals(200, con.getResponseCode());
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
