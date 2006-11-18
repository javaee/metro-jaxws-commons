import org.apache.xbean.spring.context.impl.XBeanXmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import junit.framework.TestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class ToyTest extends TestCase {

    public void test1() throws Exception {
        XBeanXmlBeanFactory factory = new XBeanXmlBeanFactory(new ClassPathResource("config.xml"));
        System.out.println(factory.getBean("jax-ws.http").toString());
    }
}
