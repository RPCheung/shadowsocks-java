package io.github.rpcheung.shadowsocks.utils;

import io.github.rpcheung.shadowsocks.crypto.common.ICrypt;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class CryptUtil {



    public static byte[] encrypt(ICrypt crypt, byte[] src) {
        byte[] data;
        ByteArrayOutputStream _remoteOutStream = null;
        try {
            _remoteOutStream = new ByteArrayOutputStream();
            crypt.encrypt(src, src.length, _remoteOutStream);
            data = _remoteOutStream.toByteArray();
        } finally {
            if (_remoteOutStream != null) {
                try {
                    _remoteOutStream.close();
                } catch (IOException e) {
                    log.error("close error", e);
                }
            }
        }
        return data;
    }

    public static byte[] decrypt(ICrypt crypt, byte[] src) {
        byte[] data;
        ByteArrayOutputStream _localOutStream = null;
        try {
            _localOutStream = new ByteArrayOutputStream();
            crypt.decrypt(src, src.length, _localOutStream);
            data = _localOutStream.toByteArray();
        } finally {

            if (_localOutStream != null) {
                try {
                    _localOutStream.close();
                } catch (IOException e) {
                    log.error("close error", e);
                }
            }
        }
        return data;
    }


}
