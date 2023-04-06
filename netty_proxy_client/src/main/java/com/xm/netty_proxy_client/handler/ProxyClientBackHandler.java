package com.xm.netty_proxy_client.handler;

import com.xm.common.msg.Constants;
import com.xm.netty_proxy_client.manager.ProxyClientManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProxyClientBackHandler extends SimpleChannelInboundHandler<ByteBuf> {

    protected static Logger logger = LoggerFactory.getLogger(ProxyClientBackHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        Channel localChannel = channelHandlerContext.channel();
        Channel proxyChannel=localChannel.attr(Constants.NEXT_CHANNEL).get();
        if (proxyChannel!=null&&proxyChannel.isActive()){
            //写回服务器
            proxyChannel.writeAndFlush(ProxyClientManager.wrapProxy(byteBuf));
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel localChannel = ctx.channel();
        Channel proxyChannel=localChannel.attr(Constants.NEXT_CHANNEL).get();
        if(proxyChannel!=null&&proxyChannel.isActive()){
            //发送断开连接请求
            proxyChannel.writeAndFlush(ProxyClientManager.wrapDisConnect());
            logger.info("[本地] 地址->{}断开",localChannel.remoteAddress());
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[本地] 触发异常");
        super.exceptionCaught(ctx, cause);
        if(ctx.channel().isActive()){
            ctx.close();
        }
    }
}
