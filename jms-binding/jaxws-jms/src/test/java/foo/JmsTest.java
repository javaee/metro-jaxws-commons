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

package foo;

import junit.framework.TestCase;

import javax.xml.ws.Endpoint;
import java.util.Random;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jitendra Kotamraju
 */
public class JmsTest extends TestCase {
    public void test1() throws Exception {
        // publish my service
        int port = new Random().nextInt(10000)+10000;
        String address = "http://localhost:" + port + "/book";
        Endpoint endpoint = Endpoint.publish(address, new MyService());

        hitEndpoint(new URL(address));
        endpoint.stop();
    }

    private void hitEndpoint(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url+"?wsdl").openConnection();
        dump(con);
    }

    private void dump(HttpURLConnection con) throws IOException {
        // Check if we got the correct HTTP response code
        int code = con.getResponseCode();

        // Check if we got the correct response
        InputStream in = (code == 200) ? con.getInputStream() : con.getErrorStream();
        int ch;
        while((ch=in.read()) != -1) {
            System.out.print((char)ch);
        }
        in.close();
    }
}
