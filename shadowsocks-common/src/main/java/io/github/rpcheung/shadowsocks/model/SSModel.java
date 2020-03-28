package io.github.rpcheung.shadowsocks.model;


import io.github.rpcheung.shadowsocks.crypto.common.ICrypt;
import lombok.Data;

import java.util.List;

/**
 * Created by cheungrp on 18/7/3.
 */
@Data
public class SSModel {

    private Object channelHandlerContext;
    private Object pendingWriteQueue;
    private ICrypt crypt;
    private List<byte[]> data;
    private byte[] cacheData;
    private String host;
    private int port;
}
