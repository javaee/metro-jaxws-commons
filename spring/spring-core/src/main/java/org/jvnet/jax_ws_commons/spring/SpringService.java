package org.jvnet.jax_ws_commons.spring;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.TubelineAssembler;
import com.sun.xml.ws.api.pipe.TubelineAssemblerFactory;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.server.EndpointFactory;
import com.sun.xml.ws.server.ServerRtException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.context.ServletContextAware;
import org.xml.sax.EntityResolver;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * Endpoint. A service object and the infrastructure around it.
 *
 * @org.apache.xbean.XBean element="service" rootElement="true"
 * @author Kohsuke Kawaguchi
 */
// javadoc for this class is used to auto-generate documentation.
public class SpringService implements FactoryBean, ServletContextAware {

    @NotNull
    private Class<?> implType;

    // everything else can be null
    private Invoker invoker;
    private QName serviceName;
    private QName portName;
    private Container container;
    private SDDocumentSource primaryWsdl;
    private Collection<? extends SDDocumentSource> metadata;
    private EntityResolver resolver;

    /**
     * Either {@link TubelineAssembler} or {@link TubelineAssemblerFactory}.
     */
    private Object assembler;


    // binding.

    // either everything is null, in which case we default to SOAP 1.1 + features from annotation

    // ... or a WSBinding configured externally
    private WSBinding binding;

    // ... or a BindingID and features
    private BindingID bindingID;
    private List<WebServiceFeature> features;


    /**
     * Technically speaking, handlers belong to
     * {@link WSBinding} and as such it should be configured there,
     * but it's just more convenient to let people do so at this object,
     * because often people use a stock binding ID constant
     * instead of a configured {@link WSBinding} bean.
     */
    private List<Handler> handlers;

    private ServletContext servletContext;

    /**
     * Set automatically by Spring if JAX-WS is used inside web container.
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

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
     * Sets the bean that implements the web service methods.
     */
    public void setBean(Object sei) {
        this.invoker = InstanceResolver.createSingleton(sei).createInvoker();
        if(this.implType==null)
            // sei could be a AOP proxy, so getClass() is not always reliable.
            // so if set explicitly via setImpl, don't override that.
            this.implType = sei.getClass();
    }

    /**
     * Sets {@link Invoker} for this endpoint.
     * Defaults to {@link InstanceResolver#createDefault(Class) the standard invoker}.
     */
    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    /**
     * Sets the {@link TubelineAssembler} or {@link TubelineAssemblerFactory} instance.
     * <p>
     * This is an advanced configuration option for those who would like to control
     * what processing JAX-WS runtime performs. The default value is {@code null},
     * in which case the {@link TubelineAssemblerFactory} is looked up from the <tt>META-INF/services</tt>.
     */
    public void setAssembler(Object assembler) {
        if(assembler instanceof TubelineAssembler || assembler instanceof TubelineAssemblerFactory)
            this.assembler = assembler;
        else
            throw new IllegalArgumentException("Invalid type for assembler "+assembler);
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
    // TODO: how to set the default container?
    public void setContainer(Container container) {
        this.container = container;
    }

    /**
     * Accepts an externally configured {@link WSBinding}
     * for advanced users.
     */
    // is there a better way to do this in Spring?
    // http://opensource.atlassian.com/projects/spring/browse/SPR-2528?page=all
    // says it doesn't support method overloading, so that's out.
    public void setBinding(WSBinding binding) {
        this.binding = binding;
    }

    /**
     * Sets the binding ID, such as <tt>{@value SOAPBinding#SOAP11HTTP_BINDING}</tt>
     * or <tt>{@value SOAPBinding#SOAP12HTTP_BINDING}</tt>.
     *
     * <p>
     * If none is specified, {@link BindingType} annotation on SEI is consulted.
     * If that fails, {@link SOAPBinding#SOAP11HTTP_BINDING}.
     *
     * @see SOAPBinding#SOAP11HTTP_BINDING
     * @see SOAPBinding#SOAP12HTTP_BINDING
     * @see HTTPBinding#HTTP_BINDING
     */
    public void setBindingID(String id) {
        this.bindingID = BindingID.parse(id);
    }

    /**
     * {@link WebServiceFeature}s that are activated in this endpoint.
     */
    public void setFeatures(List<WebServiceFeature> features) {
        this.features = features;
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

    /**
     * Optional WSDL for this endpoint.
     *
     * <p>
     * Defaults to the WSDL discovered in <tt>META-INF/wsdl</tt>,
     *
     * <p>
     * It can be either {@link String},
     * {@link URL}, or {@link SDDocumentSource}.
     *
     * If this is string, {@link ServletContext} (if available) and
     * {@link ClassLoader} are searched by this path, then failing that,
     * it's treated as an absolute {@link URL}.
     */
    // TODO: how do we discover this automatically in servlet environment?
    public void setPrimaryWsdl(Object primaryWsdl) throws IOException {
        if(primaryWsdl instanceof String) {
            this.primaryWsdl = convertStringToSource((String)primaryWsdl);
        } else
        if(primaryWsdl instanceof URL) {
            this.primaryWsdl = SDDocumentSource.create((URL)primaryWsdl);
        } else
        if(primaryWsdl instanceof SDDocumentSource) {
            this.primaryWsdl = (SDDocumentSource) primaryWsdl;
        } else
            throw new IllegalArgumentException("Unknown type "+primaryWsdl);
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
            if(binding==null) {
                if(bindingID==null)
                    bindingID = BindingID.parse(implType);
                if(features==null || features.isEmpty())
                    binding = BindingImpl.create(bindingID);
                else
                    binding = BindingImpl.create(bindingID, features.toArray(new WebServiceFeature[features.size()]));
            } else {
                if(bindingID!=null)
                    throw new IllegalStateException("Both bindingID and binding are configured");
                if(features!=null)
                    throw new IllegalStateException("Both features and binding are configured");
            }

            // configure handlers. doing this here ensures
            // that we are not doing this more than once.
            if(handlers!=null) {
                List<Handler> chain = binding.getHandlerChain();
                chain.addAll(handlers);
                binding.setHandlerChain(chain);
            }

            if(primaryWsdl==null) {
                // attempt to find it on the impl class.
                String wsdlLocation = EndpointFactory.getWsdlLocation(implType);
                if (wsdlLocation != null)
                    primaryWsdl = convertStringToSource(wsdlLocation);
            }

            endpoint = WSEndpoint.create(implType,false,invoker,serviceName,portName,new ContainerWrapper(),binding,primaryWsdl,metadata,resolver,true);
        }
        return endpoint;
    }

    /**
     * Converts {@link String} into {@link SDDocumentSource}.
     *
     * See {@link #setPrimaryWsdl(Object)} for the conversion rule.
     */
    private SDDocumentSource convertStringToSource(String wsdlLocation) throws MalformedURLException {
        URL url=null;

        if(servletContext!=null)
                        // in the servlet environment, consult ServletContext so that we can load
            // WEB-INF/wsdl/... and so on.
            url = servletContext.getResource(wsdlLocation);

        if(url==null) {
            // also check a resource in classloader.
            ClassLoader cl = implType.getClassLoader();
            url = cl.getResource(wsdlLocation);
        }

        if (url==null)
            throw new ServerRtException("cannot.load.wsdl", wsdlLocation);

        return SDDocumentSource.create(url);
    }

    public boolean isSingleton() {
        return true;
    }

    public Class getObjectType() {
        return WSEndpoint.class;
    }

    private class ContainerWrapper extends Container {

        public <T> T getSPI(Class<T> spiType) {
            // allow specified TubelineAssembler to be used
            if(spiType==TubelineAssemblerFactory.class) {
                if(assembler instanceof TubelineAssemblerFactory)
                    return spiType.cast(assembler);
                if(assembler instanceof TubelineAssembler) {
                    return spiType.cast(new TubelineAssemblerFactory() {
                        public TubelineAssembler doCreate(BindingID bindingId) {
                            return (TubelineAssembler)assembler;
                        }
                    });
                }
            }
            if(container!=null)
                // delegate to the specified container
                return container.getSPI(spiType);

            return null;
        }
    }
}
