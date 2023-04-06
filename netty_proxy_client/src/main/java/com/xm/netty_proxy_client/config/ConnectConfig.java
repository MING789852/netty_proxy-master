package com.xm.netty_proxy_client.config;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.UUID;

public class ConnectConfig {
    public static String token;
    public static Integer serverPort;
    public static String serverHost;
    public static String instance;


    static {
        ResourceBundle bundle = ResourceBundle.getBundle("application");
        token = bundle.getString("c.token");
        serverPort= Integer.valueOf(bundle.getString("server.port"));
        serverHost=bundle.getString("server.host");
        instance=UUID.randomUUID().toString();
    }
}
