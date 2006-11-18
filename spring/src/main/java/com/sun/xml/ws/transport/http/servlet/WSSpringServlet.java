package com.sun.xml.ws.transport.http.servlet;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link HttpServlet} that uses
 * Spring to obtain a configured server set up, then
 * routes incoming requests to it.
 *
 * @author Kohsuke Kawaguchi
 */
public class WSSpringServlet extends HttpServlet {

    private WSServletDelegate delegate;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        // get the configured adapters from Spring
        WebApplicationContext wac = WebApplicationContextUtils
            .getRequiredWebApplicationContext(getServletContext());
        SpringBindingList list = (SpringBindingList)wac.getBean("jax-ws.http", SpringBindingList.class);

        delegate = new WSServletDelegate(list.create(),getServletContext());
    }

    protected void doPost( HttpServletRequest request, HttpServletResponse response) {
        delegate.doPost(request,response,getServletContext());
    }

    protected void doGet( HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        delegate.doGet(request,response,getServletContext());
    }

    protected void doPut( HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        delegate.doPut(request,response,getServletContext());
    }

    protected void doDelete( HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        delegate.doDelete(request,response,getServletContext());
    }
}
