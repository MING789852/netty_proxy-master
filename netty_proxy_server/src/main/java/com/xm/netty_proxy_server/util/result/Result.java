package com.xm.netty_proxy_server.util.result;

import java.io.Serializable;

/**
 * 封装返回值
 */
public class Result<T> implements Serializable{
    private Integer code;
    private String msg;
    private T data;

    public Result(Integer code,T data,String msg){
        this.code=code;
        this.data=data;
        this.msg=msg;
    }

    public static<T> Result<T> success(String msg){
        Result<T> result=new Result<>(ResultCode.SUCCESS.getCode(),null,msg);
        return result;
    }

    public static<T> Result<T> successForData(T data){
        Result<T> result=new Result<>(ResultCode.SUCCESS.getCode(),data,ResultCode.SUCCESS.getDes());
        return result;
    }

    public static<T> Result<T> error(ResultCode code,String msg){
        Result<T> result=new Result<>(code.getCode(),null,msg);
        return result;
    }

    public static<T> Result<T> error(ResultCode code){
        Result<T> result=new Result<>(code.getCode(),null,code.getDes());
        return result;
    }

    public static<T> Result<T> error(String msg){
        Result<T> result=new Result<>(ResultCode.ERROR.getCode(),null,msg);
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
