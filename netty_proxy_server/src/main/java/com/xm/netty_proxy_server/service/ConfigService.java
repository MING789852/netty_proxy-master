package com.xm.netty_proxy_server.service;

import com.alibaba.fastjson.JSONArray;
import com.xm.netty_proxy_server.vo.User;

public interface ConfigService {

    /**
     * 登录并且返回token
     * @param user
     * @return
     */
    String login(User user);

    /**
     * 开启
     * @param instance
     * @param proxyPort
     * @return
     */
    String allow(String instance,Integer proxyPort);

    /**
     * 禁用
     * @param instance
     * @param proxyPort
     * @return
     */
    String forbid(String instance,Integer proxyPort);

    /**
     * 获取所有实例
     *
     * @return
     */
    JSONArray getAllInstance();

    /**
     * 获取实例详情
     *
     * @param instance
     * @return
     */
    JSONArray getInstanceDetail(String instance);

    /**
     * 删除代理
     *
     * @param instance
     * @param proxyPort
     * @return
     */
    String removeProxy(String instance, Integer proxyPort);

    /**
     * 新增代理
     *
     * @param instance
     * @param proxyPort
     * @param localPort
     * @return
     */
    String addProxy(String instance, Integer proxyPort, Integer localPort);
}
