package com.xm.netty_proxy_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ResourceBundle;
import java.util.UUID;

public class ConnectConfig {

    public static String connectToken;


    public static Integer port;

    public static String username;


    public static String password;


    static {
        ResourceBundle bundle = ResourceBundle.getBundle("application");
        connectToken = bundle.getString("c.token");
        port= Integer.valueOf(bundle.getString("proxy.port"));
        username=bundle.getString("login.username");
        password= bundle.getString("login.password");
    }
}
