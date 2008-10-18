package org.jvnet.jax_ws_commons.transport.grizzly_lwhs.server;

import com.sun.net.httpserver.Headers;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;

import java.util.*;

/**
 * @author Jitendra Kotamraju
 */
public class LWHSResponseHeaders extends Headers {
    private GrizzlyResponse response;

    public LWHSResponseHeaders(GrizzlyResponse response) {
        this.response = response;
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(value);
    }

    @Override
    public List<String> get(Object key) {
        return super.get(key);
    }

    @Override
    public String getFirst(String key) {
        return response.getHeader(key);
    }

    @Override
    public List<String> put(String key, List<String> value) {
        for(String val : value) {
            response.addHeader(key, val);
        }
        return super.put(key, value);
    }

    @Override
    public void add(String key, String value) {
        response.addHeader(key, value);
        super.add(key, value);
    }

    @Override
    public void set(String key, String value) {
        response.addHeader(key, value);
        super.set(key, value);
    }

    @Override
    public List<String> remove(Object key) {
        //TODO how to delete a header in response
        return super.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> t) {
        // TODO
        super.putAll(t);
    }

    @Override
    public void clear() {
        // TODO
        super.clear();
    }

    @Override
    public Set<String> keySet() {
        return super.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        return super.values();
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        return super.entrySet();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
