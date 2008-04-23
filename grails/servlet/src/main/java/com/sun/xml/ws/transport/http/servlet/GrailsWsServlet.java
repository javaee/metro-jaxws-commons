/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xml.ws.transport.http.servlet;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author Martin Grebac
 */
public class GrailsWsServlet extends HttpServlet {

    private WSServletDelegate delegate;
    
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        // get the configured adapters from Spring, they may come from two different contexts
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        ApplicationContext ac = ApplicationHolder.getApplication().getParentContext();
        
        Set<SpringBinding> bindings = new LinkedHashSet<SpringBinding>();

        // backward compatibility. recognize all bindings
        Map m = ac.getBeansOfType(SpringBindingList.class);
        for (SpringBindingList sbl : (Collection<SpringBindingList>)m.values())
            bindings.addAll(sbl.getBindings());
        m = wac.getBeansOfType(SpringBindingList.class);
        for (SpringBindingList sbl : (Collection<SpringBindingList>)m.values())
            bindings.addAll(sbl.getBindings());

        bindings.addAll( ac.getBeansOfType(SpringBinding.class).values() );
        bindings.addAll( wac.getBeansOfType(SpringBinding.class).values() );

        // create adapters
        ServletAdapterList l = new ServletAdapterList();
        for (SpringBinding binding : bindings)
            binding.create(l);

        delegate = new WSServletDelegate(l,getServletContext());
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response) {
        try {
            delegate.doPost(request, response, getServletContext());
        } catch (ServletException ex) {
            Logger.getLogger(GrailsWsServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        delegate.doGet(request,response,getServletContext());
    }

    @Override
    protected void doPut( HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        delegate.doPut(request,response,getServletContext());
    }

    @Override
    protected void doDelete( HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        delegate.doDelete(request,response,getServletContext());
    }    
}
