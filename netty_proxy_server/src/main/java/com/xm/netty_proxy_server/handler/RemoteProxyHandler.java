package com.xm.netty_proxy_server.handler;

import com.xm.common.msg.Constants;
import com.xm.netty_proxy_server.boot.RemoteProxyBoot;
import com.xm.netty_proxy_server.manager.ProxyServerManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class RemoteProxyHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private String instance;

    protected static Logger logger= LoggerFactory.getLogger(RemoteProxyHandler.class);

    public RemoteProxyHandler(String instance){
        super();
        this.instance=instance;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        Channel proxyChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) proxyChannel.localAddress();

        RemoteProxyBoot remoteProxyBoot =ProxyServerManager.getServer(instance,sa.getPort());
        if (remoteProxyBoot.getStatus()==0){
            proxyChannel.close();
            logger.info("[代理服务器]实例->{},端口->{}已禁止，无法连接",instance,sa.getPort());
            return;
        }

        String sid=proxyChannel.id().asShortText();
        Integer localPort= ProxyServerManager.getLocalPort(sa.getPort());
        Channel serverManagerChannel=ProxyServerManager.getManagerChannel(instance);

        if (serverManagerChannel!=null) {
            proxyChannel.config().setOption(ChannelOption.AUTO_READ, false);

            logger.info("[代理服务器]sid->{}请求搭建连接instance->{},端口->{}",sid,instance,localPort);
            String localPortStr=String.valueOf(localPort);
            serverManagerChannel.writeAndFlush(ProxyServerManager.wrapBridge(sid,localPortStr,instance)).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    //存储代理连接
                    ProxyServerManager.addProxyChannel(instance,sid,proxyChannel);
                }
            });
        }
        super.channelActive(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel proxyChannel = ctx.channel();
        logger.info("[代理服务器]地址->{}断开",proxyChannel.remoteAddress());
        Channel proxyManagerChannel = proxyChannel.attr(Constants.NEXT_CHANNEL).get();
        if (proxyManagerChannel!=null&&proxyManagerChannel.isActive()){
            //删除代理连接
            String sid=proxyChannel.id().asShortText();
            ProxyServerManager.removeProxyChannel(instance,sid);

            proxyManagerChannel.writeAndFlush(ProxyServerManager.wrapDisconnect());
        }else {
            proxyChannel.close();
        }

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[代理服务器] 触发异常");
        super.exceptionCaught(ctx, cause);
        if(ctx.channel().isActive()){
            ctx.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf){
        Channel proxyChannel = channelHandlerContext.channel();
        Channel proxyManagerChannel = proxyChannel.attr(Constants.NEXT_CHANNEL).get();
        if (proxyManagerChannel!=null&&proxyManagerChannel.isActive()){
            proxyManagerChannel.writeAndFlush(ProxyServerManager.wrapProxy(byteBuf));
        }
    }

}
