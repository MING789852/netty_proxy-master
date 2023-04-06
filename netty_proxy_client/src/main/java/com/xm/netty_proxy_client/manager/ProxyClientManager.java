package com.xm.netty_proxy_client.manager;

import com.xm.common.msg.*;
import com.xm.netty_proxy_client.boot.ProxyClientBoot;
import com.xm.netty_proxy_client.callback.ConnectCallBack;
import com.xm.netty_proxy_client.config.ConnectConfig;
import com.xm.netty_proxy_client.handler.ProxyClientBackHandler;
import com.xm.netty_proxy_client.handler.ProxyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProxyClientManager {

    private static Logger logger= LoggerFactory.getLogger(ProxyClientManager.class);

    private static ReentrantLock reentrantLock=new ReentrantLock();
    private static Condition condition=reentrantLock.newCondition();

    //是否运行
    private static volatile Boolean isRunning = false;
    //实例名称
    private static String instance = ConnectConfig.instance;

    private static NioEventLoopGroup localWorkerGroup=new NioEventLoopGroup();
    private static NioEventLoopGroup proxyWorkerGroup=new NioEventLoopGroup();
    //连接代理端口
    private static Bootstrap proxyBootstrap=new Bootstrap();
    //连接本地端口
    private static Bootstrap localBootstrap=new Bootstrap();
    //多通道管理
    private static final int MAX_POOL_SIZE = 100;
    private static ConcurrentLinkedQueue<Channel> proxyChannelPool = new ConcurrentLinkedQueue<Channel>();

    static {
        //连接本地端口
        localBootstrap.group(localWorkerGroup)
                .channel(NioSocketChannel.class)
                .option(NioChannelOption.SO_KEEPALIVE,true)
                .option(NioChannelOption.SO_REUSEADDR,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MsgEncoder());
                        socketChannel.pipeline().addLast(new ProxyClientBackHandler());
                        //统一异常处理
                        socketChannel.pipeline().addLast(new ExceptionCaughtHandler());
                    }
                });


        proxyBootstrap.group(proxyWorkerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MLengthFieldBasedFrameDecoder());
                        socketChannel.pipeline().addLast(new MsgDecoder());
                        socketChannel.pipeline().addLast(new MsgEncoder());
                        socketChannel.pipeline().addLast(new MIdleStateHandler(0, 10, 0, TimeUnit.SECONDS));
                        socketChannel.pipeline().addLast(new ProxyClientHandler());
                        //统一异常处理
                        socketChannel.pipeline().addLast(new ExceptionCaughtHandler());
                    }
                });
    }

    public static ReentrantLock getReentrantLock() {
        return reentrantLock;
    }

    public static Condition getCondition() {
        return condition;
    }

    public static Boolean getRunning() {
        try {
            reentrantLock.lock();
            return isRunning;
        }catch (Exception e){
            logger.error("获取连接状态失败",e);
            return isRunning;
        }finally {
            reentrantLock.unlock();
        }
    }

    public static void setRunning(Boolean running) {
        try {
            reentrantLock.lock();

            isRunning = running;
            if (isRunning==false){
                condition.signalAll();
            }
        }catch (Exception e){
            logger.error("设置连接状态失败",e);
            e.printStackTrace();
        }finally {
            reentrantLock.unlock();
        }
    }

    public static String getInstance() {
        return instance;
    }


    public static ProxyMessage wrapPing(){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.PING);

        proxyMessage.setInstance(ConnectConfig.instance);
        proxyMessage.setToken(ConnectConfig.token);
        return proxyMessage;
    }

    public static ProxyMessage wrapConnect(){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.CONNECT);

        proxyMessage.setInstance(ConnectConfig.instance);
        proxyMessage.setToken(ConnectConfig.token);
        return proxyMessage;
    }

    public static ProxyMessage wrapBridgeSuccess(String sid){
        ProxyMessage proxyMessage = new ProxyMessage();
        proxyMessage.setType(ProxyMessage.BRIDGE_SUCCESS);

        proxyMessage.setSid(sid);
        proxyMessage.setInstance(ConnectConfig.instance);
        proxyMessage.setToken(ConnectConfig.token);
        return proxyMessage;
    }

    public static ProxyMessage wrapProxy(ByteBuf byteBuf){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.PROXY);

        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        proxyMessage.setData(data);
        proxyMessage.setInstance(ConnectConfig.instance);
        proxyMessage.setToken(ConnectConfig.token);

        return proxyMessage;
    }

    public static ProxyMessage wrapDisConnect(){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.DISCONNECT);

        proxyMessage.setInstance(ConnectConfig.instance);
        proxyMessage.setToken(ConnectConfig.token);
        return proxyMessage;
    }

    public static void proxyBootstrapConnect(ConnectCallBack connectCallBack){
        try {
            proxyBootstrap.connect(ConnectConfig.serverHost, ConnectConfig.serverPort).addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        logger.info("连接服务器成功");
                        Channel ctx=channelFuture.channel();
                        connectCallBack.success(ctx);
                    }else {
                        logger.error("连接服务器失败");
                        connectCallBack.failure();
                    }
                }
            }).sync();
        } catch (InterruptedException e) {
            logger.error("连接服务器失败->{}",e);
        }
    }

    public static void localBootstrapConnect(int localPort,ConnectCallBack connectCallBack){
        localBootstrap.connect("127.0.0.1",localPort).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    logger.info("[客户端] 连接本地端口->{}成功",localPort);
                    Channel ctx=channelFuture.channel();
                    connectCallBack.success(ctx);
                }else {
                    logger.info("[客户端] 连接本地端口->{}失败",localPort);
                }
            }
        });
    }


    public static void returnProxyChanel(Channel proxyChanel) {
        if (proxyChannelPool.size() > MAX_POOL_SIZE) {
            proxyChanel.close();
        } else {
            if (proxyChanel!=null&&proxyChanel.isActive()){
                proxyChanel.config().setOption(ChannelOption.AUTO_READ, true);
                proxyChannelPool.offer(proxyChanel);
            }
        }

    }

    public static void getProxyChanel(ConnectCallBack connectCallBack){
        Channel channel = proxyChannelPool.poll();
        if (channel!=null&&channel.isActive()){
            connectCallBack.success(channel);
        }else {
            proxyBootstrap.connect(ConnectConfig.serverHost,ConnectConfig.serverPort).addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()){
                        connectCallBack.success(channelFuture.channel());
                    }
                }
            });
        }

    }
}
