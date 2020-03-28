package io.github.rpcheung.shadowsocks.transport.utils;

import com.google.gson.GsonBuilder;
import io.github.rpcheung.shadowsocks.transport.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public enum ConfigUtils {
    UTILS;

    public Config loadConfig() {
        try (InputStream stream = ClassLoader.getSystemResourceAsStream("config.json")) {
            String json = IOUtils.toString(stream, "utf-8");
            return new GsonBuilder().create().fromJson(json, Config.class);
        } catch (IOException e) {
            log.error("加载配置出错:", e);
            return null;
        }
    }
}
