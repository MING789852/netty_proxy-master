package com.xm.common.msg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统一异常处理
 */
public class ExceptionCaughtHandler extends ChannelInboundHandlerAdapter {
    protected static Logger logger= LoggerFactory.getLogger(ExceptionCaughtHandler.class);
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("异常->{}",cause.getMessage());
    }
}
