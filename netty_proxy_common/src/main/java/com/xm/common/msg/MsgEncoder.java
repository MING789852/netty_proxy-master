package com.xm.common.msg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgEncoder extends MessageToByteEncoder<ProxyMessage> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ProxyMessage msg, ByteBuf byteBuf) throws Exception {
        int typeLength=1;
        byte type= msg.getType();
        //处理localPort
        String localPort=msg.getLocalPort();
        if (localPort==null){
            localPort="";
        }
        byte[] localPortBytes=localPort.getBytes();
        int localPortLength=localPortBytes.length;
        //处理instance
        String instance= msg.getInstance();
        if (instance==null){
            instance="";
        }
        byte[] instanceBytes=instance.getBytes();
        int instanceLength=instanceBytes.length;
        //处理token
        String token= msg.getToken();
        if (token==null){
            token="";
        }
        byte[] tokenBytes=token.getBytes();
        int tokenLength=tokenBytes.length;

        //处理sid
        String sid= msg.getSid();
        if (sid==null){
            sid="";
        }
        byte[] sidBytes=sid.getBytes();
        int sidLength=sidBytes.length;

        //处理data
        byte[] data = msg.getData();
        if (data==null){
            data="".getBytes();
        }
        int dataLength=data.length;

        //设置长度
        int length=typeLength
                +4+localPortLength
                +4+instanceLength
                +4+tokenLength
                +4+sidLength
                +4+dataLength;
        //设置总长度
        byteBuf.writeInt(length);
        //设置类型
        byteBuf.writeByte(type);
        //设置代理的本地端口
        byteBuf.writeInt(localPortLength);
        byteBuf.writeBytes(localPortBytes);
        //设置实例
        byteBuf.writeInt(instanceLength);
        byteBuf.writeBytes(instanceBytes);
        //设置token
        byteBuf.writeInt(tokenLength);
        byteBuf.writeBytes(tokenBytes);
        //设置sid
        byteBuf.writeInt(sidLength);
        byteBuf.writeBytes(sidBytes);
        //设置data
        byteBuf.writeInt(dataLength);
        byteBuf.writeBytes(data);
    }
}
