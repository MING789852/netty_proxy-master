package com.xm.netty_proxy_client.boot;


import com.xm.common.msg.Constants;
import com.xm.netty_proxy_client.callback.ConnectCallBack;
import com.xm.netty_proxy_client.manager.ProxyClientManager;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProxyClientBoot {
    protected static Logger logger=LoggerFactory.getLogger(ProxyClientBoot.class);

    public void run(){
        ProxyClientManager.proxyBootstrapConnect(new ConnectCallBack() {
            @Override
            public void success(Channel channel) {
                //连接成功后修改状态
                ProxyClientManager.setRunning(true);
                //设置为连接设置实例ID
                channel.attr(Constants.INSTANCE).set(ProxyClientManager.getInstance());
                //发送建立连接请求
                channel.writeAndFlush(ProxyClientManager.wrapConnect());
            }

            @Override
            public void failure() {
                try {
                    //3秒后断线重连
                    Thread.sleep(3000);
                    run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    };


    public static void main(String[] args) {
        ProxyClientBoot proxyClientBoot=new ProxyClientBoot();
        proxyClientBoot.run();
//        logger.info("初始化重连定时器");
//        //60秒重连一次
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    if (ProxyClientManager.getCurrentClient().getRunning()==false){
//                        logger.info("断线重连....");
//                        ProxyClientManager.getCurrentClient().run();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        },0, 60 * 1000);

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                ReentrantLock reentrantLock=ProxyClientManager.getReentrantLock();
                Condition condition =ProxyClientManager.getCondition();
                while (true){
                    try {
                        reentrantLock.lock();

                        condition.await();
                        if (ProxyClientManager.getRunning()==false){
                            logger.info("断线重连....");
                            proxyClientBoot.run();
                        }else {
                            logger.info("非断线重连");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        logger.error("断线重连失败",e);
                    }finally {
                        reentrantLock.unlock();
                    }
                }
            }
        });
        thread.start();
    }
}
