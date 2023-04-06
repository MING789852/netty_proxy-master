package com.xm.netty_proxy_server.exception;

public class CommonException extends RuntimeException{
    private String msg;
    private Integer code;

    public CommonException(String msg){
        super(msg);
        this.msg=msg;
    }

    public CommonException(Integer code,String msg){
        super(msg);
        this.msg=msg;
        this.code=code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
