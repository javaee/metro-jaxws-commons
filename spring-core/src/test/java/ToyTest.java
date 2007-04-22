import org.apache.xbean.spring.context.impl.XBeanXmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import junit.framework.TestCase;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.servlet.SpringBinding;

import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class ToyTest extends TestCase {

    public void test1() throws Exception {
        XBeanXmlBeanFactory factory = new XBeanXmlBeanFactory(new ClassPathResource("config.xml"));
        //Object bean = factory.getBean("jax-ws.http");
        //System.out.println(bean.toString());

        Map beans = factory.getBeansOfType(SpringBinding.class);
        System.out.println(beans);
        assertEquals(beans.size(),3);
    }
}
