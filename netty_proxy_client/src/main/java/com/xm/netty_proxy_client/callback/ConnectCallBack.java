package com.xm.netty_proxy_client.callback;


import io.netty.channel.Channel;

public interface ConnectCallBack {
    void success(Channel channel);
    void failure();
}
