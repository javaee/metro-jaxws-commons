package org.jvnet.jax_ws_commons.transport.grizzly.server;


import com.sun.grizzly.http.*;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.Queue;


/**
 * @author Jitendra Kotamraju
 */

public class JaxwsGrizzlyAsyncFilter implements AsyncFilter {

    private static final Logger LOGGER = Logger.getLogger(JaxwsGrizzlyAsyncFilter.class.getName());

    /**
     * Mapping from the request to the processing task, used by the adapter
     * to retrieve the task associated with a request.
     */
    private static Map requestToTask = new java.util.concurrent.ConcurrentHashMap();

    /**
     * List of task instances that should be handled in a synchronous manner.
     * By the adapter adding tasks to this queue it can control whether
     * a given request is dealt with in a synchronous or asynchronous fashion.
     */
    private static Queue respondSynchronously = new java.util.concurrent.ConcurrentLinkedQueue();

    /**
     * Filter implementation, invoke the BC adapter implementation.
     */
    public boolean doFilter(AsyncExecutor asyncExecutor) {

        boolean continueSynchronously = false;

        AsyncTask asyncProcessorTask = asyncExecutor.getAsyncTask();
        // In Grizzly v1, the asynchronous extenstions are tied to the DefaultProcessorTask
        DefaultProcessorTask defaultProcTask = (DefaultProcessorTask) asyncProcessorTask.getProcessorTask();
        Request req = defaultProcTask.getRequest();


        LOGGER.info("doFilter on request " + req.toString() + ", asyncProcessorTask " + asyncProcessorTask.toString());

        requestToTask.put(req, asyncProcessorTask);

        try {
            asyncProcessorTask.getProcessorTask().invokeAdapter();
            boolean wasPresent = respondSynchronously.remove(asyncProcessorTask);
            continueSynchronously = wasPresent;
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "HTTPBC-W00641.Adapter_invoke_exception", ex);
            // make sure this is removed; just in case.
            respondSynchronously.remove(asyncProcessorTask);
            continueSynchronously = true;
        } finally {
            // make sure this mapping is cleaned up; just in case
            requestToTask.remove(req);
        }

        LOGGER.info("Continue synchronously flag set to " + continueSynchronously);


        return continueSynchronously;
    }

    /**
     * Mark request as responding synchronously, from the same thread as the request thread.
     */
    public static void finishResponseSynchronously(AsyncTask asyncProcessorTask) {

        if (asyncProcessorTask != null) {
            DefaultProcessorTask task = (DefaultProcessorTask) asyncProcessorTask.getProcessorTask();
            AsyncHandler asyncHandler = task.getAsyncHandler();
            asyncHandler.removeFromInterruptedQueue(asyncProcessorTask);

            // Mark task as synchronous
            respondSynchronously.add(asyncProcessorTask);
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "Marking exchange as synchronous");
            }
        }

    }

    /**
     * Finish the response asynchronously, i.e. from a different thread than the request thread.
     */
    public static void finishResponse(AsyncTask asyncProcessorTask) {
        if (asyncProcessorTask != null) {
            // In Grizzly v1, the asynchronous extenstions are tied to the DefaultProcessorTask
            DefaultProcessorTask task = (DefaultProcessorTask) asyncProcessorTask.getProcessorTask();

            if (task != null) {
                AsyncHandler asyncHandler = task.getAsyncHandler();
                if (asyncHandler != null) {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Finish response for asyncProcessorTask "
                                + asyncProcessorTask.toString());
                    }
                    asyncHandler.handle(asyncProcessorTask);
                } else {
                    LOGGER.log(Level.SEVERE, "HTTPBC-E00642.No_response_handler_for_request");
                }
            } else {
                LOGGER.log(Level.WARNING, "HTTPBC-W00642.No_correlating_request_for_response");
            }
        } else {
            LOGGER.log(Level.WARNING, "HTTPBC-W00643.Null_response");
        }
    }

    /**
     * Remove the task mapping for a given request
     * @param request the request
     * @return the task if there was a mapping for the request, null if not
     */
    public static AsyncTask removeTaskMapping(GrizzlyRequest request) {
        return (AsyncTask) requestToTask.remove(request);
    }
}
