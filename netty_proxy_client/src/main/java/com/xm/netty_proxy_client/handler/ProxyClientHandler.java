package com.xm.netty_proxy_client.handler;


import com.xm.common.msg.Constants;
import com.xm.common.msg.ProxyMessage;
import com.xm.netty_proxy_client.callback.ConnectCallBack;
import com.xm.netty_proxy_client.config.ConnectConfig;
import com.xm.netty_proxy_client.manager.ProxyClientManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyClientHandler extends SimpleChannelInboundHandler<ProxyMessage> {

    protected static Logger logger= LoggerFactory.getLogger(ProxyClientHandler.class);

    /**
     * 如果10s没有收到写请求，则向服务端发送心跳请求
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if(IdleState.WRITER_IDLE.equals(event.state())) {
                ctx.writeAndFlush(ProxyClientManager.wrapPing()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE) ;
            }
        }
        super.userEventTriggered(ctx, evt);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String instance=ctx.channel().attr(Constants.INSTANCE).get();
        //判断连接是否为客户端连接
        if (ConnectConfig.instance.equals(instance)){
            //设置客户端状态为断开
            ProxyClientManager.setRunning(false);
            logger.info("[客户端] 实例->{}断开与服务器的连接",instance,ctx.channel().remoteAddress());
        }
        super.channelInactive(ctx);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) throws Exception {
        Channel proxyManagerChannel=channelHandlerContext.channel();
        if (ProxyMessage.BRIDGE==proxyMessage.getType()){
            //唯一的识别ID
            String sid= proxyMessage.getSid();
            //需要建立连接的端口
            Integer localPort= Integer.valueOf(proxyMessage.getLocalPort());

            ProxyClientManager.localBootstrapConnect(localPort, new ConnectCallBack() {
                @Override
                public void success(Channel localChannel) {
                    // 连接本地端口成功后，暂时不读取数据，等代理连接创建成功
                    localChannel.config().setOption(ChannelOption.AUTO_READ, false);

                    ProxyClientManager.getProxyChanel(new ConnectCallBack() {
                        public void success(Channel proxyManagerChannel) {
                            logger.info("[客户端] 代理连接ID->{}创建成功",proxyManagerChannel.id().asShortText());
                            //绑定连接
                            proxyManagerChannel.attr(Constants.NEXT_CHANNEL).set(localChannel);
                            localChannel.attr(Constants.NEXT_CHANNEL).set(proxyManagerChannel);
                            //发送代理连接创建成功
                            proxyManagerChannel.writeAndFlush(ProxyClientManager.wrapBridgeSuccess(sid));
                            localChannel.config().setOption(ChannelOption.AUTO_READ, true);
                        }

                        @Override
                        public void failure() {

                        }
                    });
                }

                @Override
                public void failure() {

                }
            });
        }
        if (ProxyMessage.PROXY==proxyMessage.getType()){
            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
            buf.writeBytes(proxyMessage.getData());
            //获取代理连接绑定的本地连接
            Channel localChannel=proxyManagerChannel.attr(Constants.NEXT_CHANNEL).get();
            //接收到的信息转发到真实端口
            if (localChannel!=null&&localChannel.isActive()){
                localChannel.writeAndFlush(buf);
            }
        }
        if (ProxyMessage.DISCONNECT==proxyMessage.getType()){
            //获取代理连接绑定的本地连接
            Channel localChannel=proxyManagerChannel.attr(Constants.NEXT_CHANNEL).get();
            logger.info("[客户端] 释放代理连接ID->{}",proxyManagerChannel.id().asShortText());
            ProxyClientManager.returnProxyChanel(proxyManagerChannel);
            if (localChannel!=null&&localChannel.isActive()){
                // 数据发送完成后再关闭连接，解决http1.0数据传输问题
                localChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[客户端] 触发异常");
        super.exceptionCaught(ctx, cause);
        if(ctx.channel().isActive()){
            ctx.close();
        }
    }

}
