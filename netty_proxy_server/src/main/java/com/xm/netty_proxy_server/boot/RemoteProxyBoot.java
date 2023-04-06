package com.xm.netty_proxy_server.boot;


import com.xm.common.msg.ExceptionCaughtHandler;
import com.xm.common.msg.MsgEncoder;
import com.xm.netty_proxy_server.handler.RemoteProxyHandler;
import com.xm.netty_proxy_server.manager.ProxyServerManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteProxyBoot {

    private String instance;

    private Integer proxyPort;

    private Integer localPort;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private ServerBootstrap serverBootstrap;

    private Integer status;

    protected static Logger logger= LoggerFactory.getLogger(RemoteProxyBoot.class);


    public RemoteProxyBoot(String instance, Integer proxyPort, Integer localPort) {
        this.instance = instance;
        this.proxyPort = proxyPort;
        this.localPort = localPort;
    }

    public void run(){
        serverBootstrap=new ServerBootstrap();
        bossGroup=new NioEventLoopGroup();
        workerGroup=new NioEventLoopGroup();
        try {
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
//                    .option(ChannelOption.SO_BACKLOG,100)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new MsgEncoder());
                            socketChannel.pipeline().addLast(new RemoteProxyHandler(instance));
                            socketChannel.pipeline().addLast(new ExceptionCaughtHandler());
                        }
                    });
            ChannelFuture channelFuture=serverBootstrap.bind(proxyPort);
            //添加端口映射
            ProxyServerManager.addPortMapping(proxyPort,localPort);
            //设置代理服务器可用状态
            this.setStatus(1);
            channelFuture.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void allow(){
        this.setStatus(1);
    }

    public void forbid(){
        this.setStatus(0);
    }

    public void stop(){
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        //删除端口映射
        ProxyServerManager.removeProxyPort(proxyPort);
    }

    public Integer getStatus() {
        synchronized (status){
            return status;
        }
    }

    public void setStatus(Integer status) {
        synchronized (status){
            this.status = status;
        }
    }
}
