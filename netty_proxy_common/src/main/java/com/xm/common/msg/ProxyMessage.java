package com.xm.common.msg;

public class ProxyMessage {

    /**
     * 连接
     */
    public static final byte CONNECT = 0x01;

    /**
     * 断开连接
     */
    public static final byte DISCONNECT = 0x02;
    /**
     * 代理
     */
    public static final byte PROXY=0x03;

    /**
     * 心跳检测
     */
    public static final byte PING=0x04;


    /**
     * 建立连接
     */
    public static final byte BRIDGE=0x05;


    /**
     * 建立连接成功
     */
    public static final byte BRIDGE_SUCCESS=0x06;


    /**
     * 数据
     */
    private byte[] data;

    /**
     * 消息类型
     */
    private byte type;

    /**
     * 实例ID
     */
    private String instance;

    /**
     * token
     */
    private String token;

    /**
     * 代理服务器的ChannelId
     */
    private String sid;

    /**
     * 开启代理的本地端口
     */
    private String localPort;


    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getLocalPort() {
        return localPort;
    }

    public void setLocalPort(String localPort) {
        this.localPort = localPort;
    }
}
