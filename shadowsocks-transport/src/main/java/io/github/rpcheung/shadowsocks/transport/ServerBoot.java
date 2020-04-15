package io.github.rpcheung.shadowsocks.transport;

import io.github.rpcheung.shadowsocks.transport.server.ShadowsocksServer;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ServerBoot {

    public static void main(String[] args) throws Exception {
        new ShadowsocksServer().start();
    }


}
