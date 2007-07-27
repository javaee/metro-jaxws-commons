package org.jvnet.jax_ws_commons.thread_scope;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.server.AbstractMultiInstanceResolver;


/**
 * Instance resolver that creates an endpoint instance for every thread.
 *
 * @author Jitendra Kotamraju
 */
public class ThreadScopeInstanceResolver<T> extends AbstractMultiInstanceResolver<T> {
    private final ThreadLocal<T> instance = new ThreadLocal<T>();

    public ThreadScopeInstanceResolver(@NotNull Class<T> clazz) {
        super(clazz);
    }

    public @NotNull T resolve(Packet request) {
        T o = instance.get();
        if (o == null) {
            o = create();
            instance.set(o);
        }
        return o;
    }
}
