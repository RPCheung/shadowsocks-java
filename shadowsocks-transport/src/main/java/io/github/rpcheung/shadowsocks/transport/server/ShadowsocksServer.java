package io.github.rpcheung.shadowsocks.transport.server;


import io.github.rpcheung.shadowsocks.transport.codec.HostDecoder;
import io.github.rpcheung.shadowsocks.transport.common.SSContextUtils;
import io.github.rpcheung.shadowsocks.transport.config.Config;
import io.github.rpcheung.shadowsocks.transport.utils.ConfigUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.internal.SocketUtils;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.net.InetSocketAddress;

@Slf4j
@SuppressWarnings("restriction")
public class ShadowsocksServer {

    private final ServerBootstrap bootstrap = new ServerBootstrap();

    private static Config config = ConfigUtils.UTILS.loadConfig();


    static {

        if (config == null) {
            throw new IllegalArgumentException("config is null.");
        }

        SSContextUtils.CONTEXT.putAttribute("eventExecutors",
                new EpollEventLoopGroup(config.getMaxConnectionThread()));
        SSContextUtils.CONTEXT.putAttribute("config", config);

        Signal.handle(new Signal("KILL"), signal -> stop());
        Signal.handle(new Signal("TERM"), signal -> stop());
    }


    private static EventLoopGroup bossGroup;


    private static EventLoopGroup workerGroup;


    private static EventLoopGroup eventExecutors;

    public void start() {
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(EpollServerSocketChannel.class)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                    .childOption(ChannelOption.AUTO_READ, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);

            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.pipeline().addLast("hostDecoder", new HostDecoder());
                }
            });

            InetSocketAddress inetSocketAddress = SocketUtils.socketAddress(config.getServerAddress(), config.getServerPort());
            log.info("Started !  Port :" + config.getServerPort() + " Ip :" + inetSocketAddress.getHostString());

            ChannelFuture channelFuture = bootstrap.bind(inetSocketAddress);
            // 阻塞直到 channel 关闭
            channelFuture.sync().channel().closeFuture().sync();


        } catch (Exception e) {
            log.error("start error", e);
        } finally {
            stop();
        }
    }

    public static void stop() {
        try {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            eventExecutors.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            log.error("Stop Server!", e);
        }
        log.info("Stop Server!");
    }
}
