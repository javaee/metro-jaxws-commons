package test;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback.CertificateValidator;
import com.sun.xml.wss.impl.callback.CertificateValidationCallback.CertificateValidationException;

import java.security.cert.X509Certificate;

/**
 * @author Kohsuke Kawaguchi
 */
public class ValidatorImpl implements CertificateValidator {
    public boolean validate(X509Certificate certificate) throws CertificateValidationException {
        return true;
    }
}
