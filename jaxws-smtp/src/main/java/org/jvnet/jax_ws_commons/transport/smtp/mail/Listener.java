package org.jvnet.jax_ws_commons.transport.smtp.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Listens to the incoming e-mail messages and pass it to
 * {@link EmailEndpoint}.
 *
 * <p>
 * Derived classes are expected to provide the actual implementation of the listeneing logic.
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public abstract class Listener {
    protected Listener() {}

    /**
     * {@link EmailEndpoint} associated to this listener.
     */
    private EmailEndpoint endPoint;

    /**
     * Invoked when a {@link Listener} is added to {@link EmailEndpoint}.
     */
    protected void setEndPoint(EmailEndpoint ep) {
        if(this.endPoint!=null)
            throw new IllegalStateException("this listener is already registered with an endpoint");
        this.endPoint = ep;
    }

    /**
     * Gets the {@link EmailEndpoint} associated with this {@link Listener}.
     *
     * @return
     *      null if no association is made yet.
     */
    public EmailEndpoint getEndPoint() {
        return endPoint;
    }

    /**
     * Derived classes should call this method when
     * a new e-mail is received.
     *
     * This method can be invoked from any thread.
     */
    protected void handleMessage(MimeMessage msg) throws MessagingException {
        endPoint.handleMessage(msg);
    }

    protected abstract void start();

    protected abstract void stop();
}

