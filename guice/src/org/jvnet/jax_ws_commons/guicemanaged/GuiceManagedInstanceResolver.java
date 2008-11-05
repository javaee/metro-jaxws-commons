package org.jvnet.jax_ws_commons.guicemanaged;

import com.sun.xml.ws.server.AbstractMultiInstanceResolver;
import com.sun.xml.ws.api.message.Packet;
import com.sun.istack.NotNull;
import com.google.inject.Injector;
import com.google.inject.Guice;
import com.google.inject.Module;

/**
 * The instance resolver
 *
 * Looks at the endpoint class and gets the annotation in order to know what
 * guice module to use when injecting the dependencies into the endpoint.
 * 
 *
 * @author Marcus Eriksson, krummas@gmail.com
 * @since Nov 4, 2008
 */
public class GuiceManagedInstanceResolver<T> extends AbstractMultiInstanceResolver<T> {
    //private T instance=null;
    private Injector injector=null;
    public GuiceManagedInstanceResolver(@NotNull Class<T> clazz)
    {
        super(clazz);

        Class<? extends Module> moduleClass = clazz.getAnnotation(GuiceManaged.class).module();
        try {
            injector = Guice.createInjector(moduleClass.newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace(); 
        } catch (IllegalAccessException e) {
           e.printStackTrace();
        }
    }

    /**
     * Let guice create the instance
     *
     * the {@code create()} method in {@code AbstractMultiInstanceResolver} simply returns {@code clazz.newInstance()}
     * so no magic happens there.
     *
     * If the endpoint is declared as singleton, the same instance will be returned every time.
     *
     * @param packet
     * @return
     */
    public T resolve(@NotNull Packet packet) {
        return injector.getInstance(this.clazz);
    }
}
