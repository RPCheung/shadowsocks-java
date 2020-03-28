package io.github.rpcheung.shadowsocks.transport.proxy;

import io.github.rpcheung.shadowsocks.crypto.common.ICrypt;
import io.github.rpcheung.shadowsocks.model.SSModel;
import io.github.rpcheung.shadowsocks.transport.common.ClientProxy;
import io.github.rpcheung.shadowsocks.utils.CryptUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 接受客户端代理发送来的消息
 */
public class ProxyHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ProxyHandler.class);
    private final ICrypt _crypt;
    private final AtomicReference<Channel> remoteChannel = new AtomicReference<>(null);
    private final CountDownLatch latch = new CountDownLatch(1);
    private ChannelHandlerContext channelHandlerContext;


    public ProxyHandler(SSModel ssModel) {

        // 防止 channel 断开 无法 释放 byteBuf
        this._crypt = ssModel.getCrypt();
        this.channelHandlerContext = (ChannelHandlerContext) ssModel.getChannelHandlerContext();
        AttributeKey<SSModel> ss_model = AttributeKey.valueOf("ss.model");
        Attribute<SSModel> ssModelAttribute = this.channelHandlerContext.channel().attr(ss_model);
        ssModelAttribute.setIfAbsent(ssModel);
        init(ssModel.getHost(), ssModel.getPort());
        sendData(ssModel.getCacheData(), Boolean.TRUE, channelHandlerContext.channel().id().asLongText());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ctx.pipeline().remove("hostDecoder");
    }

    private synchronized void init(final String host, final int port) {

        try {
            ChannelFuture future = ClientProxy.CLIENT.connect(host, port);
            // 保存客户端连接
            future.addListener(arg0 -> {
                if (future.isSuccess()) {
                    remoteChannel.compareAndSet(null, future.channel());
                    AttributeKey<ChannelHandlerContext> serverChannel = AttributeKey.valueOf("server.channel");
                    Attribute<ChannelHandlerContext> channelAttribute = future.channel().attr(serverChannel);
                    channelAttribute.set(channelHandlerContext);
                } else {
                    future.cancel(true);
                }
                latch.countDown();
            });
            latch.await();
        } catch (Exception e) {
            logger.error("connect intenet error", e);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        ByteBuf buff = (ByteBuf) msg;
        if (buff.readableBytes() <= 0) {
            return;
        }

        byte[] data = new byte[buff.writerIndex()];
        buff.readBytes(data);
        sendData(CryptUtil.decrypt(_crypt, data), Boolean.FALSE, ctx.channel().id().asLongText());
        buff.release();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("ClientProxyHandler channelInactive close client address={}", ctx.channel().remoteAddress());
        ctx.close();
        if (remoteChannel.get() != null) {
            remoteChannel.get().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("ClientProxyHandler error client address=" + ctx.channel().remoteAddress(), cause);
        ctx.close();
        if (remoteChannel.get() != null) {
            remoteChannel.get().close();
        }
    }

    private void sendData(byte[] data, boolean isFlush, String channelId) {
        if (remoteChannel.get() != null && remoteChannel.get().isActive()) {
            logger.info("from: {} ,TSN: {}", remoteChannel.get().remoteAddress().toString(), channelId);
            if (isFlush) {
                remoteChannel.get().writeAndFlush(Unpooled.copiedBuffer(data));
            } else {
                remoteChannel.get().write(Unpooled.copiedBuffer(data));
            }

        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        remoteChannel.get().flush();
    }
}
