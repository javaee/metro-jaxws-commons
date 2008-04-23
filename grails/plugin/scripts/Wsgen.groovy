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

/**
 * Gant script that runs wsgen targets for all entries in sun-jaxws.xml file
 * @author Martin Grebac
 * @version 0.1
 */

Ant.property(environment:"env")                             
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"    
includeTargets << new File ( "${grailsHome}/scripts/Compile.groovy" )  
includeTargets << new File ( "${grailsHome}/scripts/_PackagePlugins.groovy" ) 
                              
target ('default':'''Calls wsgen task for all endpoint entries in web-app/WEB-INF/sun-jaxws.xml file

Examples: 
grails wsgen
''') {
    depends(checkVersion, compile)
    wsgen()
}

target (wsgen: "The implementation of wsgen target") {

    Ant.taskdef(name : 'wsgen',
                classname : 'com.sun.tools.ws.ant.WsGen')
        
    Ant.mkdir(dir:'${basedir}/web-app/WEB-INF/wsdl')
    Ant.mkdir(dir:"${userHome}/.grails/${grailsVersion}/projects/${baseName}/generated-java-source")

    def reader  = new FileReader("${basedir}/web-app/WEB-INF/sun-jaxws.xml")
    def doc     = DOMBuilder.parse(reader)
    def endpoints = doc.documentElement
    
    use (DOMCategory) {
        def endpointset = endpoints.'endpoint';
        endpointset.each { endpoint -> processEndpoint(endpoint) }
    }
        
}
 
def processEndpoint(endpoint) {
    def name = endpoint.'@name'
    def impl = endpoint.'@implementation'
    def url  = endpoint.'@url-pattern'
    
    println "Processing endpoint: " + name + " " + impl + " " + url-pattern
    Ant.wsgen(resourcedestdir:"${basedir}/web-app/WEB-INF/wsdl",
          sei:impl,
          keep:"true",
          sourcedestdir:"${userHome}/.grails/${grailsVersion}/projects/${baseName}/generated-java-source",
          destdir:"${userHome}/.grails/${grailsVersion}/projects/${baseName}/classes",
          genwsdl:"true",
          classpath:"${userHome}/.grails/${grailsVersion}/projects/${baseName}/classes")
}
