package org.jvnet.jax_ws_commons.spring;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import org.springframework.beans.factory.FactoryBean;
import org.xml.sax.EntityResolver;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import java.util.Collection;
import java.util.List;

/**
 * Wraps {@link WSEndpoint}.
 *
 * @org.apache.xbean.XBean element="service"
 * @author Kohsuke Kawaguchi
 */
public class SpringService implements FactoryBean {

    @NotNull
    private Class<?> implType;

    // everything else can be null
    private Invoker invoker;
    private QName serviceName;
    private QName portName;
    private Container container;
    private WSBinding binding;
    private SDDocumentSource primaryWsdl;
    private Collection<? extends SDDocumentSource> metadata;
    private EntityResolver resolver;

    /**
     * Technically speaking, handlers belong to
     * {@link WSBinding} and as such it should be configured there,
     * but it's just more convenient to let people do so at this object,
     * because often people use a stock binding ID constant
     * instead of a configured {@link WSBinding} bean.
     */
    private List<Handler> handlers;

    ///**
    // * @org.apache.xbean.Property alias="clazz"
    // */
    // I wanted to use alias="class", but @class is reserved in Spring, apparently
    public void setImpl(Class implType) {
        this.implType = implType;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    public void setPortName(QName portName) {
        this.portName = portName;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    /**
     * Accepts a configured {@link WSBinding}, a {@link BindingID},
     * or {@link String} that represents the binding ID constant.
     */
    // is there a better way to do this in Spring?
    // http://opensource.atlassian.com/projects/spring/browse/SPR-2528?page=all
    // says it doesn't support method overloading, so that's out.
    public void setBinding(Object binding) {
        if(binding instanceof String) {
            this.binding = BindingID.parse((String)binding).createBinding();
            return;
        }
        if(binding instanceof BindingID) {
            this.binding = ((BindingID) binding).createBinding();
            return;
        }
        if(binding instanceof WSBinding) {
            this.binding = (WSBinding) binding;
            return;
        }

        throw new IllegalArgumentException("Unsupported binding type "+binding.getClass());
    }


    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }

    public void setPrimaryWsdl(SDDocumentSource primaryWsdl) {
        this.primaryWsdl = primaryWsdl;
    }

    public void setMetadata(Collection<? extends SDDocumentSource> metadata) {
        this.metadata = metadata;
    }

    public void setResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Lazily created {@link WSEndpoint} instance.
     */
    private WSEndpoint<?> endpoint;

    public Object getObject() throws Exception {
        if(endpoint==null) {
            if(handlers!=null) {
                // configure handlers. doing this here ensures
                // that we are not doing this more than once.
                List<Handler> chain = binding.getHandlerChain();
                chain.addAll(handlers);
                binding.setHandlerChain(chain);
            }
            endpoint = WSEndpoint.create(implType,false,invoker,serviceName,portName,container,binding,primaryWsdl,metadata,resolver,true);
        }
        return endpoint;
    }

    public boolean isSingleton() {
        return true;
    }

    public Class getObjectType() {
        return WSEndpoint.class;
    }
}
