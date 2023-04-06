package com.xm.netty_proxy_server.manager;

import com.xm.common.msg.ProxyMessage;
import com.xm.netty_proxy_server.boot.RemoteProxyBoot;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class ProxyServerManager {

    protected static Logger logger= LoggerFactory.getLogger(ProxyServerManager.class);

    /**
     * key：instance
     * 连接管理映射
     */
    private static ConcurrentHashMap<String,Channel> managerChannel=new ConcurrentHashMap<>();

    /**
     * key1: instance
     * key2: channelId
     * 数据转发
     */
    private static ConcurrentHashMap<String,ConcurrentHashMap<String,Channel>>  proxyChannel=new ConcurrentHashMap<>();

    /**
     * 端口映射(key：代理端口  value：本地端口)
     */
    private static ConcurrentHashMap<Integer,Integer>  portMapping=new ConcurrentHashMap<>();

    /**
     * key1: instance
     * key2: 远程服务端口
     * 实例对应的代理服务器
     */
    private static ConcurrentHashMap<String,ConcurrentHashMap<Integer,RemoteProxyBoot>> proxyServer=new ConcurrentHashMap<>();

    public static ProxyMessage wrapPing(){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.PING);
        return proxyMessage;
    }

    public static ProxyMessage wrapBridge(String sid,String localPort,String instance){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.BRIDGE);

        proxyMessage.setSid(sid);
        proxyMessage.setLocalPort(localPort);
        proxyMessage.setInstance(instance);
        return proxyMessage;
    }

    public static ProxyMessage wrapProxy(ByteBuf byteBuf){
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);

        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.PROXY);
        proxyMessage.setData(data);
        return proxyMessage;
    }

    public static ProxyMessage wrapDisconnect(){
        ProxyMessage proxyMessage=new ProxyMessage();
        proxyMessage.setType(ProxyMessage.DISCONNECT);
        return proxyMessage;
    }


    public static void addServer(String instance, Integer port, RemoteProxyBoot boot){
        synchronized (proxyServer){
            ConcurrentHashMap<Integer,RemoteProxyBoot> ins=proxyServer.get(instance);
            if (ins==null){
                ins=new ConcurrentHashMap<>();
            }
            ins.put(port,boot);
            proxyServer.put(instance,ins);
            logger.info("[实例-代理端口-代理服务器]新增代理服务器,实例->{},端口->{},实例数量->{},代理服务器数量->{}",instance,port,proxyServer.size(),ins.size());
        }
    }

    public static void removeServer(String instance,Integer port){
        synchronized (proxyServer){
            //先判断实例对应的服务是否存在
            ConcurrentHashMap<Integer,RemoteProxyBoot> ins=proxyServer.get(instance);
            if (ins==null){
                return;
            }
            //关闭服务
            RemoteProxyBoot remoteProxyBoot=ins.get(port);
            if (remoteProxyBoot!=null){
                remoteProxyBoot.stop();
            }
            //删除服务
            ins.remove(port);
            if (ins.size()==0){
                proxyServer.remove(instance);
            }
            logger.info("[实例-代理端口-代理服务器]关闭代理服务器,实例->{},端口->{},实例数量->{},代理服务器数量->{}",instance,port,proxyServer.size(),ins.size());
        }
    }

    public static void removeAllServer(String instance){
        synchronized (proxyServer){
            ConcurrentHashMap<Integer,RemoteProxyBoot> ins=proxyServer.get(instance);
            if (ins!=null){
                for (Integer port:ins.keySet()){
                    RemoteProxyBoot remoteProxyBoot=ins.get(port);
                    remoteProxyBoot.stop();
                }
                proxyServer.remove(instance);
                logger.info("[实例-代理端口-代理服务器]关闭代理服务器,实例->{},实例数量->{},代理服务器数量->{}",instance,proxyServer.size(),ins.size());
            }
        }
    }

    public static ConcurrentHashMap<Integer,RemoteProxyBoot> getServers(String instance){
        synchronized (proxyServer){
            ConcurrentHashMap<Integer,RemoteProxyBoot> ins=proxyServer.get(instance);
            return ins;
        }
    }

    public static RemoteProxyBoot getServer(String instance,Integer port){
        synchronized (proxyServer){
            ConcurrentHashMap<Integer,RemoteProxyBoot> ins=proxyServer.get(instance);
            if (ins==null){
                return null;
            }
            return ins.get(port);
        }
    }

    public static Channel  getProxyChannel(String instance,String sid){
        synchronized (proxyChannel){
            ConcurrentHashMap<String,Channel> concurrentHashMap=proxyChannel.get(instance);
            if (concurrentHashMap!=null){
                return concurrentHashMap.get(sid);
            }else {
                return null;
            }
        }
    }

    public static void addProxyChannel(String instance,String sid,Channel channel){
        synchronized (proxyChannel){
            ConcurrentHashMap<String,Channel> concurrentHashMap=proxyChannel.get(instance);
            if (concurrentHashMap==null){
                concurrentHashMap=new ConcurrentHashMap<>();
            }
            concurrentHashMap.put(sid,channel);
            proxyChannel.put(instance,concurrentHashMap);
            logger.info("[实例-代理连接]新增代理连接，实例数量->{}，代理连接数量->{}",proxyChannel.size(),concurrentHashMap.size());
        }
    }

    public static void removeProxyChannel(String instance,String sid){
        synchronized (proxyChannel){
            ConcurrentHashMap<String,Channel> concurrentHashMap=proxyChannel.get(instance);
            if (concurrentHashMap!=null){
                //移除连接
                concurrentHashMap.remove(sid);
                if (concurrentHashMap.size()==0){
                    proxyChannel.remove(instance);
                }
            }
            logger.info("[实例-代理连接]移除代理连接，实例数量->{}，代理连接数量->{}",proxyChannel.size(),concurrentHashMap.size());
        }
    }

    public static void removeAllProxyChannel(String instance){
        synchronized (proxyChannel){
            proxyChannel.remove(instance);
            logger.info("[实例-代理连接]移除代理连接，实例数量->{}，代理连接数量->{}",proxyChannel.size(),0);
        }
    }

    public static Integer getLocalPort(Integer proxyPort){
        return portMapping.get(proxyPort);
    }

    public static void addPortMapping(Integer proxyPort,Integer localPort){
        portMapping.put(proxyPort,localPort);
        logger.info("[代理端口-本地端口]新增端口映射，端口映射数量->{}",portMapping.size());
    }

    public static void removeProxyPort(Integer proxyPort){
        portMapping.remove(proxyPort);
        logger.info("[代理端口-本地端口]移除端口映射，端口映射数量->{}",portMapping.size());
    }


    public static Channel getManagerChannel(String instance){
        return managerChannel.get(instance);
    }

    public static ConcurrentHashMap<String, Channel> getManagerChannelMap() {
        return managerChannel;
    }

    public static void addManagerChannel(String instance,Channel channel){
        managerChannel.put(instance,channel);
        logger.info("[实例-管理连接]新增管理连接，管理连接数量->{}",managerChannel.size());
    }

    public static void  removeManagerChannel(String instance){
        managerChannel.remove(instance);
        logger.info("[实例-管理连接]移除管理连接，管理连接数量->{}",managerChannel.size());
    }

}
