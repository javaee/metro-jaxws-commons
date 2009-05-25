import com.sun.xml.ws.commons.EC2;
import com.sun.xml.ws.commons.ec2.AmazonEC2PortType;
import com.sun.xml.ws.commons.ec2.DescribeImagesOwnerType;
import com.sun.xml.ws.commons.ec2.DescribeImagesOwnersType;
import com.sun.xml.ws.commons.ec2.DescribeImagesResponseInfoType;
import com.sun.xml.ws.commons.ec2.DescribeImagesType;
import com.sun.xml.ws.transport.http.client.HttpTransportPipe;
import junit.framework.TestCase;

import java.io.File;

/**
 * @author Kohsuke Kawaguchi
 */
public class FooTest extends TestCase {
    public void test1() throws Exception {
//        Endpoint e = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, new EchoService());
//        e.publish("http://127.0.0.1:12345/");

        HttpTransportPipe.dump = true;

        final String userHome = System.getProperty("user.home");
        final File home = new File(userHome, ".ec2");

        AmazonEC2PortType p = EC2.connect(new File(home, "pk.pem"), new File(home, "cert.pem"));

//        ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,"http://localhost:12345/");
        
        final DescribeImagesResponseInfoType response = p.describeImages(
                new DescribeImagesType().withOwnersSet(
                    new DescribeImagesOwnersType().withItem(
                        new DescribeImagesOwnerType().withOwner("amazon")
                    )
                )).getImagesSet();
        System.out.println(response.getItem());
    }
}