package com.depository_manage.exception;


import lombok.Getter;

/**
 * 自定义异常类
 */
@Getter
public class MyException extends RuntimeException {
    /**
     * 异常码
     */
    private int code;
    /**
     * 异常信息
     */
    private String msg;

    public MyException(int code, String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public MyException(String msg){
        super(msg);
        this.msg = msg;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
