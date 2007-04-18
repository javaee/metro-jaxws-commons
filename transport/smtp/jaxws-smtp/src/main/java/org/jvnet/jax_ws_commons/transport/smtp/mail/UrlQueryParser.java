package org.jvnet.jax_ws_commons.transport.smtp.mail;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses the query part of the URL.
 *
 * @author Kohsuke Kawaguchi
 */
public class UrlQueryParser {
    private final Map<String,String> values = new HashMap<String,String>();

    /**
     * Parses the query string.
     *
     * @param queryPart
     *      string like "a=b&amp;c=d". May contain escape like '%8D'.
     */
    public UrlQueryParser(String queryPart) {
        if(queryPart==null)
            return;     // nothing to parse, but that's not an error
        try {
            for( String token : queryPart.split("&") ) {
                int idx = token.indexOf('=');
                if(idx<0) {
                    values.put(token,"");
                } else {
                    values.put(
                        token.substring(0,idx),
                        URLDecoder.decode(token.substring(idx+1),"UTF-8"));
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(); // impossible
        }
    }

    /**
     * Parses the query string.
     *
     * @param uri
     *      URI whose query part will be parsed
     */
    public UrlQueryParser(URI uri) {
        this(fixNull(uri.getQuery()));
    }

    /**
     * Gets the value for the specified key.
     *
     * <p>
     * For example, if the query string was "a=b&amp;c=d",
     * you get "b" from {@code getValue("a")}.
     *
     * @return
     *      null if the value is not found for the key.
     */
    public String getValue(String key) {
        return getValue(key,null);
    }

    /**
     * Gets the value for the specified key.
     *
     * <p>
     * For example, if the query string was "a=b&amp;c=d",
     * you get "b" from {@code getValue("a")}.
     *
     * @param defaultValue
     *      if no value was found for the given key,
     *      this value will be returned.
     */
    public String getValue(String key,String defaultValue) {
        String value = values.get(key);
        if(value==null) value = defaultValue;
        return value;
    }

    /**
     * Gets the value for the specified key as an integer.
     *
     * <p>
     * For example, if the query string was "a=b&amp;c=d",
     * you get "b" from {@code getValue("a")}.
     *
     * @param defaultValue
     *      if no value was found for the given key,
     *      this value will be returned.
     * @throws NumberFormatException
     *      if the value was found but not a number.
     */
    public int getValue(String key, int defaultValue) throws NumberFormatException {
        String value = values.get(key);
        if(value==null)     return defaultValue;
        return Integer.parseInt(value);
    }

    /**
     * Returns true if the query has a parameter of the given name.
     */
    public boolean has(String key) {
        return values.containsKey(key);
    }

    /**
     * Adds all the key/value pairs into the given map.
     */
    public void addTo(Map<? super String,? super String> map) {
        map.putAll(values);
    }

    private static String fixNull(String s) {
        if(s==null) return "";
        return s;
    }
}
