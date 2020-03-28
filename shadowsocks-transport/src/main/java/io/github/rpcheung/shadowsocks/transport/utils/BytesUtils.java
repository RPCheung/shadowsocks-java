package io.github.rpcheung.shadowsocks.transport.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public enum BytesUtils {

    utils;

    public byte[] byteBuf2Bytes(ByteBuf byteBuf) {
        byte[] data = new byte[byteBuf.readableBytes()];
        if (byteBuf.readableBytes() == 0) {
            return new byte[0];
        }
        byteBuf.readBytes(data);
        return data;
    }

    public ByteBuf bytes2ByteBuf(byte[] data) {
        return Unpooled.copiedBuffer(data);
    }

    public static void releaseByteBufAllRefCnt(ByteBuf buf) {
        synchronized (BytesUtils.class) {
            if (buf == null) {
                return;
            }
            int ref = buf.refCnt();
            if (ref > 0) {
                buf.release(ref);
            }
        }
    }
}
