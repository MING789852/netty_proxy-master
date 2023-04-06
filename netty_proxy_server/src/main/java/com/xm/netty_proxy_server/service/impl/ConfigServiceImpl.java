package com.xm.netty_proxy_server.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xm.netty_proxy_server.annotation.RequireToken;
import com.xm.netty_proxy_server.boot.RemoteProxyBoot;
import com.xm.netty_proxy_server.config.ConnectConfig;
import com.xm.netty_proxy_server.exception.CommonException;
import com.xm.netty_proxy_server.manager.ProxyServerManager;
import com.xm.netty_proxy_server.service.ConfigService;
import com.xm.netty_proxy_server.util.token.TokenUtils;
import com.xm.netty_proxy_server.vo.User;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfigServiceImpl implements ConfigService {

    @Override
    public String login(User user) {
        String username=user.getUsername();
        String password=user.getPassword();

        if (StringUtils.isEmpty(username)){
            throw new CommonException("账号不能为空");
        }
        if (StringUtils.isEmpty(password)){
            throw new CommonException("密码不能为空");
        }

        String configUsername=ConnectConfig.username;
        String configPassword=ConnectConfig.password;
        if (configUsername.equals(username)){
            try {
                if (configPassword.equals(password)){
                    return TokenUtils.getToken(username,password);
                }else {
                    throw new CommonException("账号密码错误，请重试");
                }
            } catch (Exception e) {
                throw new CommonException(e.getMessage());
            }
        }else {
            throw new CommonException("账号密码错误，请重试");
        }
    }

    @Override
    public String allow(String instance, Integer proxyPort) {
        RemoteProxyBoot proxyBoot=ProxyServerManager.getServer(instance,proxyPort);
        proxyBoot.allow();
        return "操作成功";
    }

    @Override
    public String forbid(String instance, Integer proxyPort) {
        RemoteProxyBoot proxyBoot=ProxyServerManager.getServer(instance,proxyPort);
        proxyBoot.forbid();
        return "操作成功";
    }

    @Override
    public JSONArray getAllInstance() {
        JSONArray jsonArray = new JSONArray();
        ConcurrentHashMap<String, Channel> managerChannelMap = ProxyServerManager.getManagerChannelMap();
        if (managerChannelMap == null) {
            return jsonArray;
        }
        for (String instance : managerChannelMap.keySet()) {
            JSONObject data = new JSONObject();
            ConcurrentHashMap<Integer, RemoteProxyBoot> servers = ProxyServerManager.getServers(instance);
            data.put("instance", instance);
            if (servers == null) {
                data.put("count", 0);
            } else {
                data.put("count", servers.size());
            }
            Channel channel=ProxyServerManager.getManagerChannel(instance);
            data.put("address",channel.remoteAddress().toString());
            jsonArray.add(data);
        }
        return jsonArray;
    }

    @Override
    public JSONArray getInstanceDetail(String instance) {
        JSONArray jsonArray = new JSONArray();
        ConcurrentHashMap<Integer, RemoteProxyBoot> servers = ProxyServerManager.getServers(instance);
        if (servers == null) {
            return jsonArray;
        }
        for (Map.Entry<Integer, RemoteProxyBoot> server : servers.entrySet()) {
            Integer proxyPort = server.getKey();
            Integer localPort = ProxyServerManager.getLocalPort(proxyPort);
            RemoteProxyBoot remoteProxyBoot = server.getValue();
            JSONObject data = new JSONObject();
            data.put("proxyPort", proxyPort);
            data.put("localPort", localPort);
            data.put("status", remoteProxyBoot.getStatus());
            data.put("instance",instance);
            jsonArray.add(data);
        }

        return jsonArray;
    }

    @Override
    public String removeProxy(String instance, Integer proxyPort) {
        //删除端口映射和netty服务
        ProxyServerManager.removeServer(instance, proxyPort);
        return "操作成功";
    }

    @Override
    public String addProxy(String instance, Integer proxyPort, Integer localPort) {
        Channel channel = ProxyServerManager.getManagerChannel(instance);
        if (channel == null) {
            throw new  CommonException("未找到实例");
        }
        Integer port = ProxyServerManager.getLocalPort(proxyPort);
        if (port != null) {
            throw new  CommonException("端口被占用");
        }
        RemoteProxyBoot remoteProxyBoot = new RemoteProxyBoot(instance, proxyPort, localPort);
        ProxyServerManager.addServer(instance, proxyPort, remoteProxyBoot);
        remoteProxyBoot.run();
        return "操作成功";
    }
}
