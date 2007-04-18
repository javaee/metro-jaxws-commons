package org.jvnet.jax_ws_commons.transport.smtp.mail;


import javax.mail.internet.MimeMessage;

/**
 * Callback interface that receives e-mails
 *
 * @see EmailEndpoint#setNewMailHandler(NewMailHandler)
 * @author Kohsuke Kawaguchi
 */
public interface NewMailHandler {
    /**
     * Called for each new e-mail.
     *
     * @param mail
     *      represents a received e-mail.
     *      always non-null.
     * @throws Exception
     *      if the method throws any {@link Throwable},
     *      it will be simply logged.
     */
    void onNewMail(MimeMessage mail) throws Exception;
}
