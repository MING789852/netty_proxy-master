package com.xm.netty_proxy_server.controller;

import com.alibaba.fastjson.JSONArray;
import com.xm.netty_proxy_server.annotation.RequireToken;
import com.xm.netty_proxy_server.service.ConfigService;
import com.xm.netty_proxy_server.util.result.Result;
import com.xm.netty_proxy_server.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("config")
public class ConfigController {
    @Autowired
    private ConfigService configService;

    @GetMapping("addProxy")
    @RequireToken
    public Result<String> addProxy(String instance, Integer proxyPort, Integer localPort) {
        return Result.successForData(configService.addProxy(instance, proxyPort, localPort));
    }

    @GetMapping("removeProxy")
    @RequireToken
    public Result<String> removeProxy(String instance, Integer proxyPort) {
        return Result.successForData(configService.removeProxy(instance, proxyPort));
    }

    @GetMapping("getAllInstance")
    @RequireToken
    public Result<JSONArray> getAllInstance() {
        return Result.successForData(configService.getAllInstance());
    }

    @GetMapping("getInstanceDetail")
    @RequireToken
    public Result<JSONArray> getInstanceDetail(String instance) {
        return Result.successForData(configService.getInstanceDetail(instance));
    }

    @PostMapping("login")
    public Result<String> login(@RequestBody User user){
        return Result.successForData(configService.login(user));
    }

    @GetMapping("forbid")
    @RequireToken
    public Result<String> forbid(String instance, Integer proxyPort){
        return Result.successForData(configService.forbid(instance, proxyPort));
    }

    @GetMapping("allow")
    @RequireToken
    public Result<String> allow(String instance, Integer proxyPort){
        return Result.successForData(configService.allow(instance, proxyPort));
    }
}
