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

import javax.jws.WebService;
import javax.jws.WebParam;
import javax.xml.ws.BindingType;
import org.jvnet.jax_ws_commons.jms.JMSBindingID;

/**
 *
 * @author Jitendra Kotamraju
 */
@WebService
@BindingType(JMSBindingID.SOAP12JMS_BINDING)
public class MyService {

    public Book get(@WebParam(name="p1") int p1, @WebParam(name="p2") String p2) {
        return new Book(p1,p2);
    }

    public static final class Book {
        public int id = 1;
        public String title = "Java";
        public int p1;
        public String p2;
        public String nullValue = null;
        public float floatValue = 1.23456f;
        public double doubleValue = 1.23456;
        public boolean booleanValue = true;

        public Book() {
        }

        public Book(int p1, String p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

}
