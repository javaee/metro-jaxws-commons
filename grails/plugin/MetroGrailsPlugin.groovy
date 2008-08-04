/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

import org.codehaus.groovy.grails.compiler.support.*
import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory
import groovy.xml.StreamingMarkupBuilder

class MetroGrailsPlugin {
//    def dependsOn = []
	
    def author = "Martin Grebac"
    def authorEmail = "martin.grebac@sun.com"
    def title = "Add METRO Web Service support to services built with Grails framework."
    def description = '''\
METRO plugin allows applications developed with Grails framework to expose service 
classes as SOAP Web Services. It uses the SOAP implementation from METRO (includes JAX-WS).
'''
    def documentation = "http://jax-ws-commons.dev.java.net/grails/"
    def version = '1.0.2'
    def loadAfter = ['services']    
    def watchedResources = ["file:./grails-app/services/**/*Service.groovy",
                            "file:./plugins/*/grails-app/services/**/*Service.groovy",
                            "file:./src/java/**/*.java",
                            "file:./src/groovy/**/*.groovy"
                           ]

    def doWithSpring = {
        File f = new File("grails-app/conf/spring/resources.xml")
        println "Generating " + f

        def output = new FileOutputStream (f)

        StreamingMarkupBuilder smb = new StreamingMarkupBuilder();
        output << smb.bind {
            namespaces << [wss:"http://jax-ws.dev.java.net/spring/servlet"]
            namespaces << [ws: "http://jax-ws.dev.java.net/spring/core"]
            namespaces << [sp: "http://www.springframework.org/schema/beans"]
            namespaces << [xsi:"http://www.w3.org/2001/XMLSchema-instance"]

            String userHome = System.getProperty("user.home")
            String appName = application.getMetadata().get("app.name")

            String grailsVersion = grails.util.GrailsUtil.getGrailsVersion();

            String classDir = "${userHome}/.grails/${grailsVersion}/projects/${appName}/classes"
            String sourceDir = "${userHome}/.grails/${grailsVersion}/projects/${appName}/generated-java-source"
            String wsdlDir = "web-app/WEB-INF/wsdl"
            
            sp.beans('xsi:schemaLocation':"http://jax-ws.dev.java.net/spring/core http://jax-ws.dev.java.net/spring/core.xsd http://jax-ws.dev.java.net/spring/servlet http://jax-ws.dev.java.net/spring/servlet.xsd http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd") {
                application?.allClasses.each { cl ->
                    java.lang.annotation.Annotation a = cl?.getAnnotation(javax.jws.WebService.class)
                    if (a) {
                        def String shortName = cl.name
                        println "Generating spring configuration for: ${shortName}"

                        if (shortName?.contains('.')) {
                            shortName = shortName.substring(shortName.lastIndexOf('.') + 1, shortName.size())
                        }

                        wss.binding(url:"/services/${shortName}") {
                            wss.service {
                                ws.service(bean:"#${shortName}")
                            }
                        }
                        sp.bean(id:"${shortName}", 'class':"${cl.name}")

                        new File(wsdlDir).mkdir()
                        
                        String[] attrs = ["-classpath", 
                                          classDir,
                                          "-d",
                                          classDir,
                                          "-keep",
                                          "-wsdl",
                                          "-r",
                                          wsdlDir,
                                          "-s",
                                          sourceDir,
                                          "${cl.name}"
                                          ]
                        com.sun.tools.ws.WsGen.doMain(attrs);
                    }
                }
            }
        }
        output.close()
    }

    def doWithApplicationContext = { ctx ->

//        /*
//        <beans xmlns="http://www.springframework.org/schema/beans"
//          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//          xmlns:ws="http://jax-ws.dev.java.net/spring/core"
//          xmlns:wss="http://jax-ws.dev.java.net/spring/servlet">
//
//            <wss:binding url="/testurl">
//              <wss:service>
//                  <ws:service bean="#testService"/>
//              </wss:service>
//            </wss:binding> 
//            <bean id="testService" class="foo.TestJavaService" />
//            
//        </beans>
//        */
//        
    }

    def doWithWebDescriptor = { xml ->
        
        def jaxwsdesc = new File("web-app/WEB-INF/sun-jaxws.xml")

        def servlets = xml.servlet[0]
        def servletMappings = xml.'servlet-mapping'[0]
        def listeners = xml.listener[0]

        if (jaxwsdesc.exists()) {
            
            //plain jax-ws way
            def reader  = new FileReader(jaxwsdesc)

            def doc     = DOMBuilder.parse(reader)
            def endpoints = doc.documentElement

            listeners + {
                listener {
                  'listener-class'('com.sun.xml.ws.transport.http.servlet.WSServletContextListener')
                }
            }

            use (DOMCategory) {
                def endpointset = endpoints.'endpoint'
                endpointset.each { endpoint -> processEndpoint(endpoint, servlets, servletMappings) }
            }
            
        } else {
            
            // spring-way
            println "Creating jax-ws spring configuration in web.xml."
    
            servlets + {
                servlet{
                    'servlet-name'('jaxws-servlet')
                    'servlet-class'("com.sun.xml.ws.transport.http.servlet.GrailsWsServlet") //WSSpringServlet
                }
            }

            servletMappings + {
                'servlet-mapping' {
                    'servlet-name'('jaxws-servlet')
                    'url-pattern'('/services/*')
                }
            }

        }
        
    }

    def processEndpoint(endpoint, servlets, servletMappings) {
        def name = endpoint.'@name'
        def impl = endpoint.'@implementation'
        def url  = endpoint.'@url-pattern'
        
        println "Processing web.xml for endpoint: " + name + " | " + impl + " | " + url-pattern

        // Pure jax-ws way
        servlets + {
            servlet{
                'servlet-name'(name)
                'servlet-class'("com.sun.xml.ws.transport.http.servlet.WSServlet")
                'load-on-startup'(1)
            }
        }
                
        servletMappings + {
            'servlet-mapping' {
                'servlet-name'(name)
                'url-pattern'(url)
            }
        }

    }
    
    def doWithDynamicMethods = { ctx -> 
//        println "DOWITHDYNMETHODS" + ctx
    }
	
    def onChange = { event -> 
//        println "ONCHANGE" + event
    }
                                                                                  
    def onApplicationChange = { event -> 
//        println "ONAPPCHANGE" + event        
    }
}
