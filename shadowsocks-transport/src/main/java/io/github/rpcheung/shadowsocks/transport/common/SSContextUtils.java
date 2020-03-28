package io.github.rpcheung.shadowsocks.transport.common;

import com.google.common.collect.Maps;

import java.util.Map;

public enum SSContextUtils {

    CONTEXT;

    private Map<String, Object> attribute = Maps.newConcurrentMap();

    public <T> T getAttribute(String name, Class<T> clazz) {
        return clazz.cast(attribute.get(name));
    }

    public <T> T removeAttribute(String name, Class<T> clazz) {
        return clazz.cast(attribute.remove(name));
    }

    public void putAttribute(String name, Object value) {
        attribute.put(name, value);
    }
}
