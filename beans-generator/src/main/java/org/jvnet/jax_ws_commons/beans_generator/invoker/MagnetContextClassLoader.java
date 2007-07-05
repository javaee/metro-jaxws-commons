/**
 * Copyright (c) 2006-2007, Magnetosoft, LLC
 * All rights reserved.
 * 
 * Licensed under the Magnetosoft License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.magnetosoft.ru/LICENSE
 *
 * file: MagnetContextClassLoader.java
 */

package org.jvnet.jax_ws_commons.beans_generator.invoker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class loader with reverse delegation model of class resolving.
 * It tries to load class and define classes itself, and after all - from current. 
 * It works like class loaders in application servers.
 * 
 * 
 * Created: 18.06.2007
 * @author Malyshkin Fedor (fedor.malyshkin@magnetosoft.ru)
 * @version $Revision$
 */
class MagnetContextClassLoader extends URLClassLoader {

    private Logger log = null;

    private String contextName = null;

    private ClassLoader parent = null;

    private ClassLoader system = null;

    private SecurityManager secMgr = null;

    // Packages
    private Map<String, Package> packages =
	    new LinkedHashMap<String, Package>(100);

    // Packages
    private Map<String, Class> classes = new LinkedHashMap<String, Class>(100);

    /**
     * @param contextName
     * @param baseClassLoader
     * @param ccl
     */
    public MagnetContextClassLoader(String contextName,
	    ClassLoader currentClassLoader) {
	super(new URL[0], currentClassLoader);
	this.contextName = contextName;
	system = getSystemClassLoader();
	secMgr = System.getSecurityManager();
	log =
		Logger.getLogger(MagnetContextClassLoader.class.getCanonicalName()
			+ "-" + contextName);
	this.parent = getParent();
    }

    /* (non-Javadoc)
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
	return loadClass(name, false);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
	    throws ClassNotFoundException {
	Class<?> result = null;

	// first of all looking in already loaded classes
	result = findLoadedClass(name);
	if (null != result) {
	    if (resolve) resolveClass(result);
	    return result;
	}

	// then - in system classes - we not intended to overwrite system classes
	try {
	    result = system.loadClass(name);
	    if (result != null) {
		if (resolve) resolveClass(result);
		return (result);
	    }
	} catch (ClassNotFoundException e) {
	    // Ignore
	}

	// if we are intended to use our own classes- check permissions for it
	if (secMgr != null) {
	    int i = name.lastIndexOf('.');
	    if (i >= 0) {
		try {
		    secMgr.checkPackageAccess(name.substring(0, i));
		} catch (SecurityException se) {
		    String error =
			    "Security Violation, attempt to use "
				    + "Restricted Class: " + name;
		    log.log(Level.WARNING, error, se);
		    throw new ClassNotFoundException(error, se);
		}
	    }
	}

	// search...
	try {
	    result = findClass(name);
	    if (result != null) {
		if (resolve) resolveClass(result);
		return (result);
	    }
	} catch (ClassNotFoundException e) {
	    log.info("We didn't find class with that name in our CL " + name);
	    ;
	}

	ClassLoader loader = parent;
	if (loader == null) loader = system;
	try {
	    result = loader.loadClass(name);
	    if (result != null) {
		log.info("Loading class from parent: " + name);
		if (resolve) resolveClass(result);
		return result;
	    }
	} catch (ClassNotFoundException e) {
	}

	throw new ClassNotFoundException(name);
    }

    /* (non-Javadoc)
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

	String tempPath = name.replace('.', '/');
	String classPath = tempPath + ".class";

	// Looking up the package
	String packageName = null;
	int pos = name.lastIndexOf('.');
	if (pos != -1) packageName = name.substring(0, pos);
	Package pkg = null;
	if (packageName != null) {
	    pkg = getPackage(packageName);
	    // Define the package (if null)
	    if (pkg == null) definePackage(packageName);
	}

	synchronized (classes) {
	    Class<?> result = classes.get(name);
	    if (null == result) {
		byte[] classBytes = loadResource(classPath);
		if (classBytes != null) {
		    result =
			    defineClass(name, classBytes, 0, classBytes.length);
		    classes.put(name, result);
		    return result;
		} else {
		    throw new ClassNotFoundException(name);	    
		}
	    }
	    return result;
	}
    }

    protected byte[] loadResource(String path) {
	ClassLoader cl = parent;
	if (null == cl) cl = system;

	InputStream is = cl.getResourceAsStream(path);

	if (is != null) {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
		byte[] buff = new byte[1000];
		int readed = 0;
		while ((readed = is.read(buff)) != -1)
		    baos.write(buff, 0, readed);
	    } catch (IOException e) {
		log.log(Level.SEVERE, e.getMessage(), e);
		return null;
	    }

	    return baos.toByteArray();
	}
	return null;

    }

    protected Package getPackage(String name) {
	synchronized (packages) {
	    return packages.get(name);
	}
    }

    private Package definePackage(String name) {
	synchronized (packages) {
	    Package pkg = packages.get(name);
	    if (pkg == null) {
		pkg =
			definePackage(name, null, null, null, null, null, null, null);
		packages.put(name, pkg);
	    }
	    return pkg;
	}
    }

    @Override
    protected Package[] getPackages() {
	List<Package> result = new ArrayList<Package>(100);
	synchronized (packages) {
	    result.addAll(packages.values());
	}
	result.addAll(Arrays.asList(getPackages()));
	return result.toArray(new Package[0]);
    }

}
