package io.github.rpcheung.shadowsocks.transport.codec;


import io.github.rpcheung.shadowsocks.crypto.common.CryptFactory;
import io.github.rpcheung.shadowsocks.crypto.common.ICrypt;
import io.github.rpcheung.shadowsocks.model.SSModel;
import io.github.rpcheung.shadowsocks.transport.common.ReadState;
import io.github.rpcheung.shadowsocks.transport.common.SSContextUtils;
import io.github.rpcheung.shadowsocks.transport.config.Config;
import io.github.rpcheung.shadowsocks.transport.proxy.ProxyHandler;
import io.github.rpcheung.shadowsocks.transport.utils.BytesUtils;
import io.github.rpcheung.shadowsocks.utils.CryptUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socks.SocksAddressType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.List;

public class HostDecoder extends ReplayingDecoder<ReadState> {

    private Config config = SSContextUtils.CONTEXT.getAttribute("config", Config.class);
    private ICrypt _crypt = CryptFactory.factory.get(config.getMethod(), config.getPassword());
    private SSModel model = new SSModel();
    private byte domainLength;
    private SocksAddressType hostType;


    private static final Logger logger = LoggerFactory.getLogger(HostDecoder.class);

    public HostDecoder() {
        super(ReadState.HOST_TYPE);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        model.setCrypt(_crypt);
        model.setChannelHandlerContext(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buff = (ByteBuf) msg;
        byte[] d = new byte[buff.writerIndex()];
        buff.readBytes(d);
        ByteBuf data = Unpooled.copiedBuffer(CryptUtil.decrypt(_crypt, d));
        super.channelRead(ctx, data);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf data, List<Object> out) throws Exception {

        switch (state()) {
            case HOST_TYPE: {
                hostType = SocksAddressType.valueOf(data.readByte());
                if ((hostType != SocksAddressType.IPv4)
                        && (hostType != SocksAddressType.IPv6)
                        && (hostType != SocksAddressType.DOMAIN)) {
                    BytesUtils.releaseByteBufAllRefCnt(data);
                    logger.info("UNKNOWN.........................");
                    return;
                }
            }
            case HOST_LENGTH: {
                if (hostType == SocksAddressType.DOMAIN) {
                    domainLength = data.readByte();
                }
                checkpoint(ReadState.HOST_CONTENT);
            }
            case HOST_CONTENT: {
                if (hostType == SocksAddressType.IPv4) {
                    byte[] ipBytes = new byte[4];
                    data.readBytes(ipBytes);
                    model.setHost(Inet4Address.getByAddress(ipBytes).toString().substring(1));
                }
                if (hostType == SocksAddressType.IPv6) {
                    byte[] hostBytes = new byte[16];
                    data.readBytes(hostBytes);
                    model.setHost(Inet6Address.getByAddress(hostBytes).toString().substring(1));
                }
                if (hostType == SocksAddressType.DOMAIN) {
                    byte[] hostBytes = new byte[domainLength];
                    data.readBytes(hostBytes);
                    model.setHost(new String(hostBytes));
                }
                if (hostType == SocksAddressType.UNKNOWN) {
                    logger.info("UNKNOWN.........................");
                    BytesUtils.releaseByteBufAllRefCnt(data);
                    return;
                }
                logger.info(model.getHost());
                checkpoint(ReadState.PORT);
            }
            case PORT: {
                model.setPort(data.readShort());
                checkpoint(ReadState.DATA);
            }
            case DATA: {
                int readableLength = data.writerIndex() - data.readerIndex();
                byte[] remain = new byte[readableLength];
                data.readBytes(remain, 0, readableLength);
                model.setCacheData(remain);
                logger.info("from: {} ,TSN: {}", ctx.channel().remoteAddress().toString(),
                        ctx.channel().id().asLongText());
                ctx.pipeline()
                        .addLast(SSContextUtils.CONTEXT.getAttribute("eventExecutors", EpollEventLoopGroup.class),
                                "proxyHandler", new ProxyHandler(model));
                checkpoint(ReadState.HOST_TYPE);

            }
        }
    }
}
