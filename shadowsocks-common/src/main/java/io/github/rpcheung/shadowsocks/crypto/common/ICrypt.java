package io.github.rpcheung.shadowsocks.crypto.common;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * crypt 加密
 * 
 * @author
 * 
 */
public interface ICrypt extends Serializable{

	// AEAD 用
	void isForUdp(boolean isForUdp);
	
	void encrypt(byte[] data, ByteArrayOutputStream stream);

	void encrypt(byte[] data, int length, ByteArrayOutputStream stream);

	void decrypt(byte[] data, ByteArrayOutputStream stream);

	void decrypt(byte[] data, int length, ByteArrayOutputStream stream);

}
