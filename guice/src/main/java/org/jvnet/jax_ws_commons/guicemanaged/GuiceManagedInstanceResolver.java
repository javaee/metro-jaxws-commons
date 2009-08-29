package org.jvnet.jax_ws_commons.guicemanaged;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.ResourceInjector;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSWebServiceContext;
import com.sun.xml.ws.server.AbstractMultiInstanceResolver;
import org.guiceyfruit.Injectors;
import org.guiceyfruit.support.CloseFailedException;

import javax.xml.ws.WebServiceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * The instance resolver
 * <p/>
 * Looks at the endpoint class and gets the annotation in order to know what
 * guice module to use when injecting the dependencies into the endpoint.
 *
 * @author Marcus Eriksson, krummas@gmail.com
 * @since Nov 4, 2008
 */
public class GuiceManagedInstanceResolver<T> extends AbstractMultiInstanceResolver<T> {

    private static Injector injector;

    private ResourceInjector resourceInjector;

    private WSWebServiceContext webServiceContext;

    public GuiceManagedInstanceResolver(@NotNull final Class<T> clazz)
            throws IllegalAccessException, InstantiationException {
        super(clazz);
    }

    /* (non-Javadoc)
      * @see com.sun.xml.ws.api.server.InstanceResolver#dispose()
      */
    @Override
    public void dispose() {

        try {
            Injectors.close(GuiceManagedInstanceResolver.injector);
        } catch (CloseFailedException e) {
            //if context closing failed, create a hard fail exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Create an injector, adds a module for the web service context instance
     *
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private Injector getInjector() throws IllegalAccessException, InstantiationException {

        final List<Module> moduleInstances = new ArrayList<Module>();
        final Class<? extends Module>[] moduleClasses = this.clazz.getAnnotation(GuiceManaged.class).module();

        for (final Class<? extends Module> moduleClass : moduleClasses) {
            moduleInstances.add(moduleClass.newInstance());
        }

        moduleInstances.add(new AbstractModule() {

            /* (non-Javadoc)
                * @see com.google.inject.AbstractModule#configure()
                */
            @Override
            protected void configure() {
                this.bind(WebServiceContext.class).toInstance(GuiceManagedInstanceResolver.this.webServiceContext);
            }
        });

        return Guice.createInjector(moduleInstances);
    }

    /**
     * Let guice create the instance
     * <p/>
     * the {@code create()} method in {@code AbstractMultiInstanceResolver}
     * simply returns {@code clazz.newInstance()} so no magic happens there.
     * <p/>
     * If the endpoint is declared as singleton, the same instance will be
     * returned every time.
     *
     * @param packet
     * @return
     */
    @Override
    public T resolve(@NotNull final Packet packet) {
        final T instance = GuiceManagedInstanceResolver.injector.getInstance(this.clazz);
        this.resourceInjector.inject(this.webServiceContext, instance);
        return instance;
    }

    /**
     * save the web service context instance
     *
     * @param wsc
     * @param endpoint
     */
    @Override
    public void start(final WSWebServiceContext wsc, final WSEndpoint endpoint) {
        super.start(wsc, endpoint);

        this.resourceInjector = GuiceManagedInstanceResolver.getResourceInjector(endpoint);
        this.webServiceContext = wsc;

        try {
            //use double checked locking to ensure as close to a singleton instance as possible without static initialization
            if (GuiceManagedInstanceResolver.injector == null) {
                synchronized (GuiceManagedInstanceResolver.class) {
                    if (GuiceManagedInstanceResolver.injector == null) {
                        GuiceManagedInstanceResolver.injector = this.getInjector();
                    }
                }
            }
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Illegal access in the endpoint", e);
        } catch (final InstantiationException e) {
            throw new RuntimeException("Could not instantiate the endpoint", e);
		}
	}
}
