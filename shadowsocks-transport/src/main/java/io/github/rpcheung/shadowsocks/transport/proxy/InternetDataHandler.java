package io.github.rpcheung.shadowsocks.transport.proxy;

import io.github.rpcheung.shadowsocks.crypto.common.ICrypt;
import io.github.rpcheung.shadowsocks.model.SSModel;
import io.github.rpcheung.shadowsocks.utils.CryptUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 接受互联网消息处理
 */
@Sharable
public class InternetDataHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(InternetDataHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        // ctx.channel().writeAndFlush(Unpooled.copiedBuffer(getCacheData(ctx)));
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        ChannelHandlerContext clientProxyChannel = getClientProxyChannel(ctx);
        ICrypt _crypt = getCrypt(ctx);

        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] data = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(byteBuf);
        if (_crypt == null) {
            throw new IllegalStateException("miss parameter");
        }
        byte[] deData = CryptUtil.encrypt(_crypt, data);
        Channel channel = clientProxyChannel.channel();

        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(Unpooled.copiedBuffer(deData));
        } else {
            ctx.close();
        }

        byteBuf.release();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        Channel channel = getClientProxyChannel(ctx).channel();

        // 等待 发送缓冲区 的 数据发送完 为减少 Connection reset by peer
        while ((channel != null) && (channel.isActive()) && (!(channel.unsafe().outboundBuffer().isEmpty()))) {
            Thread.sleep(2000);
        }

        ctx.close();
        getClientProxyChannel(ctx).close();
        logger.info("InternetDataHandler channelInactive close  Interview address = {}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        ctx.close();
        getClientProxyChannel(ctx).close();
        logger.error("InternetDataHandler error Interview address = " + ctx.channel().remoteAddress(), cause);
    }

    private ChannelHandlerContext getClientProxyChannel(ChannelHandlerContext ctx) {
        AttributeKey<ChannelHandlerContext> serverChannel = AttributeKey.valueOf("server.channel");
        Attribute<ChannelHandlerContext> channelAttribute = ctx.channel().attr(serverChannel);
        return channelAttribute.get();
    }

    private ICrypt getCrypt(ChannelHandlerContext ctx) {
        AttributeKey<SSModel> ssModel = AttributeKey.valueOf("ss.model");
        Optional<ChannelHandlerContext> channelHandlerContextOptional = Optional.ofNullable(getClientProxyChannel(ctx));
        if (!channelHandlerContextOptional.isPresent()) {
            return null;
        }
        Optional<Channel> channelOptional = Optional.ofNullable(getClientProxyChannel(ctx).channel());
        if (!channelOptional.isPresent()) {
            return null;
        }
        Attribute<SSModel> ssModelAttribute = getClientProxyChannel(ctx).channel().attr(ssModel);
        return ssModelAttribute.get().getCrypt();
    }
}
