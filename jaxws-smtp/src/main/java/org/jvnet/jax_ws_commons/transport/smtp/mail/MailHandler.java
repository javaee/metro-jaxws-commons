package org.jvnet.jax_ws_commons.transport.smtp.mail;

import javax.mail.internet.MimeMessage;

/**
 * Callback that will be called to give access the the mail received by the {@link org.jvnet.jax_ws_commons.transport.smtp.mail.Listener}
 * @author Vivek Pandey
 */
public interface MailHandler {
    void onNewMail(MimeMessage message);
}
