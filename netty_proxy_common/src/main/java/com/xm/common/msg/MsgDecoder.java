package com.xm.common.msg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class MsgDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //不够4个字节直接扔掉
        if(byteBuf.readableBytes() < 4){
            return;
        }
        //获取总长度
        int length=byteBuf.readInt();
        if (byteBuf.readableBytes()<length){
            return;
        }
        ProxyMessage msg=new ProxyMessage();
        //获取类型
        byte type=byteBuf.readByte();
        msg.setType(type);
        //获取代理的本地端口
        int localPortLength=byteBuf.readInt();
        byte[] localPortBytes=new byte[localPortLength];
        byteBuf.readBytes(localPortBytes);
        msg.setLocalPort(new String(localPortBytes));
        //获取实例
        int instanceLength=byteBuf.readInt();
        byte[] instanceBytes=new byte[instanceLength];
        byteBuf.readBytes(instanceBytes);
        msg.setInstance(new String(instanceBytes));
        //获取token
        int tokenLength=byteBuf.readInt();
        byte[] tokenBytes=new byte[tokenLength];
        byteBuf.readBytes(tokenBytes);
        msg.setToken(new String(tokenBytes));
        //获取sid
        int sidLength=byteBuf.readInt();
        byte[] sidBytes=new byte[sidLength];
        byteBuf.readBytes(sidBytes);
        msg.setSid(new String(sidBytes));
        //获取data
        int dataLength=byteBuf.readInt();
        byte [] dataBytes=new byte[dataLength];
        byteBuf.readBytes(dataBytes);
        msg.setData(dataBytes);

        list.add(msg);
    }
}
