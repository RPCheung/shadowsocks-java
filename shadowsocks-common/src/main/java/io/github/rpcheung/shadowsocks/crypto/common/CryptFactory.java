package io.github.rpcheung.shadowsocks.crypto.common;

import com.google.common.collect.Maps;
import io.github.rpcheung.shadowsocks.crypto.component.CryptPlugin;
import io.github.rpcheung.shadowsocks.utils.JarUtils;
import io.github.rpcheung.shadowsocks.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Map;

@Slf4j
public enum CryptFactory {

    factory;

    private final Map<String, Class<? extends ICrypt>> crypts = Maps.newConcurrentMap();
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    CryptFactory() {
        registeringICrypt();
    }

    @SuppressWarnings("unchecked")
    private void registeringICrypt() {
        try {
            System.setProperty("shadowsocks.crypto.plugin", "C:\\Users\\rpCheung\\Desktop\\plugin");
            URL[] urls = JarUtils.utils.findJar(new File(System.getProperty("shadowsocks.crypto.plugin")));
            String[] classes = new String[0];
            for (URL url : urls) {
                JarUtils.utils.addURL(url);
                classes = JarUtils.utils.jarScanner(new File(url.getFile()));
            }
            for (String clz : classes) {
                Class<? extends ICrypt> c = (Class<? extends ICrypt>) Thread.currentThread().getContextClassLoader().loadClass(clz);
                if (c.isAnnotationPresent(CryptPlugin.class)) {
                    CryptPlugin cryptPlugin = c.getAnnotation(CryptPlugin.class);
                    for (String val : cryptPlugin.value()) {
                        crypts.put(val, c);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("init error", e);
        }
    }

    public ICrypt get(String name, String password) {
        Class<? extends ICrypt> clazz = crypts.get(name);
        if (clazz == null) {
            throw new IllegalArgumentException("找不到此算法 :" + name);
        }

        return ReflectionUtils.utils.initCryptObject(clazz, name, password);
    }


}
