package io.github.rpcheung.shadowsocks.transport.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Config {

    @SerializedName("server_address")
    private String serverAddress;

    @SerializedName("server_port")
    private int serverPort;

    private String password;

    private String method;

    @SerializedName("max_connection_thread")
    private int maxConnectionThread;

    @SerializedName("max_client_connection_thread")
    private int maxClientConnectionThread;
}
