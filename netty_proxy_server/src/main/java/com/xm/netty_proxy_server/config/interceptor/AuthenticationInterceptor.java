package com.xm.netty_proxy_server.config.interceptor;

import com.xm.netty_proxy_server.annotation.RequireToken;
import com.xm.netty_proxy_server.exception.CommonException;
import com.xm.netty_proxy_server.util.token.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class AuthenticationInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从 http 请求头中取出 token
        String token = request.getHeader("token");
        // 如果不是映射到方法直接通过
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod=(HandlerMethod)handler;
        Method method=handlerMethod.getMethod();
        //检查是否有token注释，有则校验token
        if (method.isAnnotationPresent(RequireToken.class)) {
            if (StringUtils.isEmpty(token)){
                throw new CommonException(909,"token不存在");
            }
            if (!TokenUtils.verify(token)){
                throw new CommonException(909,"token失效");
            }else {
                return true;
            }
        }else {
            return true;
        }
    }
}
