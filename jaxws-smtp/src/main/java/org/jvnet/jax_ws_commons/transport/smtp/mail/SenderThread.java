package org.jvnet.jax_ws_commons.transport.smtp.mail;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Thread} that send e-mails via SMTP.
 *
 * <p>
 * This mechanism allows multiple e-mails to be sent through one connection,
 * and thereby improve the performance.
 *
 * @author Kohsuke Kawaguchi
 */
final class SenderThread extends Thread {

    private final List<MimeMessage> msgs = new ArrayList<MimeMessage>();

    private final Session session;

    private boolean isShuttingDown = false;

    private static final Logger logger = Logger.getLogger(SenderThread.class.getName());

    public SenderThread(Session session) {
        super("SMTP sender thread");
        this.session = session;
    }

    public synchronized void queue(MimeMessage msg) {
        if(isShuttingDown)
            throw new IllegalStateException("the sender thread is shutting down");

        msgs.add(msg);
        notify();
    }

    private synchronized void waitForMessage() throws InterruptedException {
        while(msgs.isEmpty())
            this.wait();
    }

    public synchronized void shutDown() {
        if(!isShuttingDown) {
            isShuttingDown = true;
            interrupt();
        }
        try {
            join();
        } catch (InterruptedException e) {
            // process an interrupt later
            Thread.currentThread().interrupt();
        }
    }

    private synchronized boolean hasMessage() {
        return !msgs.isEmpty();
    }

    private synchronized MimeMessage getNext() {
        return msgs.remove(0);
    }

    public void run() {
        while(!isShuttingDown) {
            try {
                waitForMessage();
            } catch (InterruptedException e) {
                // going to shutdown
                assert isShuttingDown;
                if(!hasMessage())
                    return;
            }

            // send all the messages in the queue
            try {
                logger.fine(toString()+" : waking up");
                Transport t = session.getTransport("smtp");
                t.connect();
                do {
                    MimeMessage msg = getNext();
                    logger.fine(toString()+" : sending "+msg.getSubject());
                    t.sendMessage(msg,msg.getAllRecipients());
                } while(hasMessage());
                t.close();
                logger.fine(toString()+" : going back to sleep");
            } catch (MessagingException e) {
                logger.log(Level.WARNING,"Failed to send an e-mail via SMTP",e);
            }
        }
    }
}
