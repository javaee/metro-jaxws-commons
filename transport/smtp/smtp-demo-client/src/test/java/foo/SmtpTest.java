package foo;

import junit.framework.TestCase;
import javax.xml.ws.BindingProvider;
import org.jvnet.jax_ws_commons.transport.smtp.SMTPFeature;
import org.jvnet.jax_ws_commons.transport.smtp.POP3Info;
import org.jvnet.jax_ws_commons.transport.smtp.SenderInfo;
import com.sun.xml.ws.developer.WSBindingProvider;
import org.jvnet.jax_ws_commons.transport.smtp.client.SMTPTransportTube;

public class SmtpTest{

    public void testSmtp() {
        SMTPTransportTube.dump = true;		// Enable logging

        /**
         * Setup SMTP server
         */
        SMTPFeature feature = new SMTPFeature("kohsuke.sfbay.sun.com", "10025",
            "smtp.transport.client@kohsuke.org");

        //Setup POP3
        feature.setPOP3("kohsuke.org", "smtp.transport.client", "jaxws123");

        //Set the endpoint address
        BookService proxy =
            new BookServiceService().getBookServicePort(feature);
        ((WSBindingProvider)proxy).setAddress("smtp://smtp.transport.server@kohsuke.org");

        for(Book book : proxy.getRecommendedBooks()){
            System.out.println("--------------------------------");
            System.out.println("Id: " + book.getId());
            System.out.println("Author: "+book.getAuthor());
            System.out.println("Title: "+book.getTitle());
        }
        System.out.println("--------------------------------");
    }
}
