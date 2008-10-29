import com.sun.xml.ws.commons.EC2;
import com.sun.xml.ws.commons.ec2.AmazonEC2PortType;
import com.sun.xml.ws.commons.ec2.DescribeImagesOwnerType;
import com.sun.xml.ws.commons.ec2.DescribeImagesOwnersType;
import com.sun.xml.ws.commons.ec2.DescribeImagesResponseInfoType;
import com.sun.xml.ws.transport.http.client.HttpTransportPipe;
import junit.framework.TestCase;

import javax.xml.ws.Endpoint;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;

/**
 * @author Kohsuke Kawaguchi
 */
public class FooTest extends TestCase {
    public void test1() {
//        Endpoint e = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, new EchoService());
//        e.publish("http://127.0.0.1:12345/");

        HttpTransportPipe.dump = true;

        File home = new File("/home/kohsuke/.ec2/Sun");

        AmazonEC2PortType p = EC2.connect(new File(home, "pk-5242455T55VWUVLW32VDDVC7KLWW3I4L.pem"), new File(home, "cert-5242455T55VWUVLW32VDDVC7KLWW3I4L.pem"));

//        ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,"http://localhost:12345/");
        
        DescribeImagesOwnersType owners = new DescribeImagesOwnersType();
        DescribeImagesOwnerType o = new DescribeImagesOwnerType();
        o.setOwner("amazon");
        owners.getItem().add(o);

        Holder<DescribeImagesResponseInfoType> rsp = new Holder<DescribeImagesResponseInfoType>();
        p.describeImages(null, null, owners, new Holder<String>(), rsp);

        System.out.println(rsp.value);
    }
}
