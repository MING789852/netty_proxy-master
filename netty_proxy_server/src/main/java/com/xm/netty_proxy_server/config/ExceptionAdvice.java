package com.xm.netty_proxy_server.config;

import com.xm.netty_proxy_server.exception.CommonException;
import com.xm.netty_proxy_server.util.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionAdvice {
    @ExceptionHandler({CommonException.class})
    @ResponseBody
    public Result<String> handleCmException(CommonException e) {
        if (e.getCode()==null){
            return Result.error(e.getMessage());
        }else {
            return new Result<>(e.getCode(),e.getMsg(),e.getMsg());
        }
    }
}
