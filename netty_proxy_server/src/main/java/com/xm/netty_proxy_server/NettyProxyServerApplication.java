package com.xm.netty_proxy_server;

import com.xm.netty_proxy_server.boot.ProxyServerBoot;
import com.xm.netty_proxy_server.config.ConnectConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyProxyServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyProxyServerApplication.class, args);
        new ProxyServerBoot(ConnectConfig.port).run();
    }

}
