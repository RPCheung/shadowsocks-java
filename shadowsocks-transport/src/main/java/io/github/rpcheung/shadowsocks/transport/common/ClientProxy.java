package io.github.rpcheung.shadowsocks.transport.common;

import io.github.rpcheung.shadowsocks.transport.config.Config;
import io.github.rpcheung.shadowsocks.transport.proxy.InternetDataHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;

public enum ClientProxy {

    CLIENT;

    static {
        SSContextUtils.CONTEXT.putAttribute("clientProxy", CLIENT);
        bootstrap = new Bootstrap();
        Config config = SSContextUtils.CONTEXT.getAttribute("config", Config.class);
        internetEventLoopGroup = new EpollEventLoopGroup(config.getMaxClientConnectionThread());
        init();
    }


    private static Bootstrap bootstrap;

    private static EventLoopGroup internetEventLoopGroup;


    private static void init() {
        bootstrap.group(internetEventLoopGroup)
                .channel(EpollSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.AUTO_READ, true);
    }

    public ChannelFuture connect(String host, int port) {
        return bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new InternetDataHandler());
            }
        }).connect(host, port);
    }

}
