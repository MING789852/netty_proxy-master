package com.xm.netty_proxy_server.boot;

import com.xm.common.msg.*;
import com.xm.netty_proxy_server.handler.ProxyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.TimeUnit;

public class ProxyServerBoot {
    private   EventLoopGroup bossGroup=new NioEventLoopGroup();
    private   EventLoopGroup workerGroup=new NioEventLoopGroup();

    private int port;

    public ProxyServerBoot(int port) {
        this.port = port;
    }

    public void run(){
        try {
            ServerBootstrap b=new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new MLengthFieldBasedFrameDecoder());
                            socketChannel.pipeline().addLast(new MsgDecoder());
                            socketChannel.pipeline().addLast(new MsgEncoder());
                            //n秒内未收到请求，触发userEventTriggered
                            socketChannel.pipeline().addLast(new MIdleStateHandler(11, 0, 0, TimeUnit.SECONDS));
                            socketChannel.pipeline().addLast(new ProxyServerHandler());
                            socketChannel.pipeline().addLast(new ExceptionCaughtHandler());
                        }
                    })
                    .bind(port).channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
