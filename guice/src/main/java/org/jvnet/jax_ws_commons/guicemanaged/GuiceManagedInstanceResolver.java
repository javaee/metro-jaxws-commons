package org.jvnet.jax_ws_commons.guicemanaged;

import java.util.List;
import java.util.ArrayList;
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
    private final Injector injector;
    public GuiceManagedInstanceResolver(@NotNull Class<T> clazz) throws IllegalAccessException, InstantiationException 
    {
        super(clazz);
	List<Module> moduleInstances = new ArrayList<Module>();
        Class<? extends Module>[] moduleClasses = clazz.getAnnotation(GuiceManaged.class).module();
	    for(Class<? extends Module> moduleClass : moduleClasses)
	    {
		    moduleInstances.add(moduleClass.newInstance());
	    }

	    injector = Guice.createInjector(moduleInstances);
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
