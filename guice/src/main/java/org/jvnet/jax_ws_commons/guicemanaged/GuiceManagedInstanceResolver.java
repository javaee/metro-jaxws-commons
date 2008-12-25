package org.jvnet.jax_ws_commons.guicemanaged;

import java.util.List;
import java.util.ArrayList;
import com.sun.xml.ws.server.AbstractMultiInstanceResolver;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WSWebServiceContext;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.ResourceInjector;
import com.sun.istack.NotNull;
import com.google.inject.Injector;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.AbstractModule;

import javax.xml.ws.WebServiceContext;

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
    private Injector injector = null;
    private ResourceInjector resourceInjector;
    private WSWebServiceContext webServiceContext;
    public GuiceManagedInstanceResolver(@NotNull Class<T> clazz) throws IllegalAccessException, InstantiationException
    {
        super(clazz);
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
    @Override
    public T resolve(@NotNull Packet packet) {
        T instance = injector.getInstance(this.clazz);
        resourceInjector.inject(webServiceContext,instance);
        return instance;
    }

    /**
     * save the web service context instance
     * @param wsc
     * @param endpoint
     */

    @Override
    public void start(WSWebServiceContext wsc, WSEndpoint endpoint) {
        super.start(wsc,endpoint);
        resourceInjector = getResourceInjector(endpoint);
        webServiceContext=wsc;
        try {
            injector = getInjector();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access in the endpoint",e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not instantiate the endpoint",e);
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
        List<Module> moduleInstances = new ArrayList<Module>();
        Class<? extends Module>[] moduleClasses = clazz.getAnnotation(GuiceManaged.class).module();
        for(Class<? extends Module> moduleClass : moduleClasses)
        {
            moduleInstances.add(moduleClass.newInstance());
        }
        moduleInstances.add(new AbstractModule() {
            protected void configure() {
                bind(WebServiceContext.class).toInstance(webServiceContext);
            }
        });
        return Guice.createInjector(moduleInstances);
    }
}
