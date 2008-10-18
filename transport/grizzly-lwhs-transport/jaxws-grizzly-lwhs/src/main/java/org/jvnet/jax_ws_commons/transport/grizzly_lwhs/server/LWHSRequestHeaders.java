package org.jvnet.jax_ws_commons.transport.grizzly_lwhs.server;

import com.sun.net.httpserver.Headers;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;

import java.util.*;

/**
 * @author Jitendra Kotamraju
 */
public class LWHSRequestHeaders extends Headers {
    private final GrizzlyRequest request;
    private boolean useMap = false;

    public LWHSRequestHeaders(GrizzlyRequest request) {
        this.request = request;
    }

    private void convertToMap() {
        if (!useMap) {
            Enumeration e = request.getHeaderNames();
            while(e.hasMoreElements()) {
                String name = (String)e.nextElement();
                Enumeration ev = request.getHeaders(name);
                while(ev.hasMoreElements()) {
                    String value = (String)ev.nextElement();
                    super.add(name, value);
                }
            }
            useMap = true;
        }
    }

    @Override
    public int size() {
        convertToMap();
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        convertToMap();
        return super.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        return useMap ? super.containsKey(key) : request.getHeader((String)key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        convertToMap();
        return super.containsValue(value);
    }

    @Override
    public List<String> get(Object key) {
        convertToMap();
        return super.get(key);
    }

    @Override
    public String getFirst(String key) {
        return useMap ? super.getFirst(key) : request.getHeader(key);
    }

    @Override
    public List<String> put(String key, List<String> value) {
        convertToMap();
        return super.put(key, value);
    }

    @Override
    public void add(String key, String value) {
        convertToMap();
        super.add(key, value);
    }

    @Override
    public void set(String key, String value) {
        convertToMap();
        super.set(key, value);
    }
    @Override
    public List<String> remove(Object key) {
        convertToMap();
        return super.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> t) {
        convertToMap();
        super.putAll(t);
    }

    @Override
    public void clear() {
        convertToMap();
        super.clear();
    }

    @Override
    public Set<String> keySet() {
        convertToMap();
        return super.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        convertToMap();
        return super.values();
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        convertToMap();
        return super.entrySet();
    }

    @Override
    public String toString() {
        convertToMap();
        return super.toString();
    }

}
