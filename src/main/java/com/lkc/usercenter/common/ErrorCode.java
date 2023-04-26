package com.lkc.usercenter.common;

/**
 * @author lkc
 * @version 1.0
 */
public enum ErrorCode {
    SUCCESS(0,"ok",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    PARAM_NULL_ERROR(40001,"请求数据为空",""),
    NO_LOGIN(40101,"没有登录",""),
    NO_AUTH(40101,"无权限",""),
    SYSTEM_ERROR(50000,"系统内部异常","");

    /**
     * 状态码信息
     */
    private final int code;

    /**
     * 状态码详细（详情）
     */
    private final String message;

    /**
     * 状态码详细描述
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
