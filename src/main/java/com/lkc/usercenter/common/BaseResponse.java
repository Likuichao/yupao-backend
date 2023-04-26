package com.lkc.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lkc
 * @version 1.0
 * 通用返回类
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;

    private T data;

    private String message;

    private String description;

    public BaseResponse(int code, T data, String message,String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description =description;
    }

    public BaseResponse(int code, T data, String message) {
        this(code,data,message,"");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }

    public BaseResponse(ErrorCode errorCode,String description) {
        this(errorCode.getCode(),null,errorCode.getMessage(),description);
    }

    public BaseResponse(int errorCode,String message,String description) {
        this(errorCode,null,message,description);
    }

    public BaseResponse(ErrorCode errorCode,String message, String description) {
        this(errorCode.getCode(),null,message,description);
    }

}
