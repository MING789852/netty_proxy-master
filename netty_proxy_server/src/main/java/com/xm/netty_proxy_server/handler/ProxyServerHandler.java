package com.xm.netty_proxy_server.handler;

import com.xm.common.msg.Constants;
import com.xm.common.msg.ProxyMessage;
import com.xm.netty_proxy_server.config.ConnectConfig;
import com.xm.netty_proxy_server.manager.ProxyServerManager;
import com.xm.netty_proxy_server.util.GetBeanUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

public class ProxyServerHandler extends SimpleChannelInboundHandler<ProxyMessage> {


    protected static Logger logger= LoggerFactory.getLogger(ProxyServerHandler.class);

    public ProxyServerHandler(){
        super();
    }

    /**
     * 如果11s没有读请求，则向客户端发送心跳
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (IdleState.READER_IDLE.equals((event.state()))) {
                ctx.writeAndFlush(ProxyServerManager.wrapPing()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE) ;
            }
        }
    }



    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ProxyMessage msg) throws UnsupportedEncodingException {
        //验证token
        String token= msg.getToken();
        if (!ConnectConfig.connectToken.equals(token)){
            return;
        }
        //获取实例
        String instance= msg.getInstance();
        //心跳检测
        if (ProxyMessage.PING==msg.getType()){
            logger.info("[管理服务器] 收到客户端-->{}心跳",instance);
        }
        //请求连接
        if (ProxyMessage.CONNECT==msg.getType()){
            Channel manageChannel=channelHandlerContext.channel();

            if (ProxyServerManager.getServers(instance)!=null){
                channelHandlerContext.close();
                logger.error("[管理服务器] 已存在实例->{}"+instance);
            }
            logger.info("[管理服务器] 客户端实例->{}连接",instance);
            manageChannel.attr(Constants.INSTANCE).set(instance);
            ProxyServerManager.addManagerChannel(instance,manageChannel);
        }
        //代理连接成功
        if (ProxyMessage.BRIDGE_SUCCESS==msg.getType()) {
            Channel proxyManagerChannel = channelHandlerContext.channel();
            String sid = msg.getSid();
            Channel proxyChannel = ProxyServerManager.getProxyChannel(instance, sid);
            //绑定连接
            if (proxyChannel!=null&&proxyChannel.isActive()){
                proxyManagerChannel.attr(Constants.NEXT_CHANNEL).set(proxyChannel);
                proxyChannel.attr(Constants.NEXT_CHANNEL).set(proxyManagerChannel);
                proxyChannel.config().setOption(ChannelOption.AUTO_READ, true);
            }
            logger.info("[代理服务器]sid->{}搭建连接成功",sid);
        }
        //连接断开
        if (ProxyMessage.DISCONNECT==msg.getType()){
            logger.info("[管理服务器] 接收断开请求");
            Channel proxyManagerChannel=channelHandlerContext.channel();
            Channel proxyChannel=proxyManagerChannel.attr(Constants.NEXT_CHANNEL).get();
            if (proxyChannel!=null&&proxyChannel.isActive()){
                // 数据发送完成后再关闭连接，解决http1.0数据传输问题
                proxyChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
        //转发数据
        if (ProxyMessage.PROXY==msg.getType()){
            Channel proxyManagerChannel=channelHandlerContext.channel();
            Channel proxyChannel=proxyManagerChannel.attr(Constants.NEXT_CHANNEL).get();
            if (proxyChannel!=null&&proxyChannel.isActive()){
                ByteBuf buf = channelHandlerContext.alloc().buffer(msg.getData().length);
                buf.writeBytes(msg.getData());
                //获取代理端口相应数据
                proxyChannel.writeAndFlush(buf);
            }
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();

        String instance=incoming.attr(Constants.INSTANCE).get();
        if (instance!=null){
            //删除代理端口连接
            ProxyServerManager.removeAllProxyChannel(instance);
            //关闭代理连接服务器
            ProxyServerManager.removeAllServer(instance);
            //删除管理连接
            ProxyServerManager.removeManagerChannel(instance);
        }

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        if(ctx.channel().isActive()){
            ctx.close();
        }
    }
}
