package org.jvnet.jax_ws_commons.spring;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.binding.BindingImpl;
import org.springframework.beans.factory.FactoryBean;
import org.xml.sax.EntityResolver;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.BindingType;
import java.util.Collection;
import java.util.List;

/**
 * Wraps {@link WSEndpoint}.
 *
 * @org.apache.xbean.XBean element="service"
 * @author Kohsuke Kawaguchi
 */
// javadoc for this class is used to auto-generate documentation.
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
    /**
     * Fully qualified class name of the SEI class. Required.
     */
    public void setImpl(Class implType) {
        this.implType = implType;
    }

    /**
     * Sets {@link Invoker} for this endpoint.
     * Defaults to {@link InstanceResolver#createDefault(Class) the standard invoker}.
     */
    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    /**
     * Sets the service name of this endpoint.
     * Defaults to the name inferred from the impl attribute.
     */
    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Sets the port name of this endpoint.
     * Defaults to the name inferred from the impl attribute.
     */
    public void setPortName(QName portName) {
        this.portName = portName;
    }

    /**
     * Sets the custom {@link Container}. Optional.
     */
    public void setContainer(Container container) {
        this.container = container;
    }

    /**
     * Accepts a configured {@link WSBinding}, a {@link BindingID},
     * or {@link String} that represents the binding ID constant.
     *
     * <p>
     * If none is specified, {@link BindingType} annotation on SEI is consulted.
     * If that fails, {@link SOAPBinding#SOAP11HTTP_BINDING}.
     *
     * @see SOAPBinding#SOAP11HTTP_BINDING
     * @see SOAPBinding#SOAP12HTTP_BINDING
     * @see HTTPBinding#HTTP_BINDING
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

    /**
     * {@link Handler}s for this endpoint.
     * Note that the order is significant.
     *
     * <p>
     * If there's just one handler and that handler is declared elsewhere,
     * you can use this as a nested attribute like <tt>handlers="#myHandler"</tt>.
     * Or otherwise nesteed &lt;bean> or &lt;ref> tag can be used to specify
     * multiple handlers.
     */
    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }

    public void setPrimaryWsdl(SDDocumentSource primaryWsdl) {
        this.primaryWsdl = primaryWsdl;
    }

    public void setMetadata(Collection<? extends SDDocumentSource> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the {@link EntityResolver} to be used for resolving schemas/WSDLs
     * that are referenced. Optional.
     */
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

                if(binding==null)
                    binding = BindingImpl.create(BindingID.parse(implType));


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
