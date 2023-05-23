package com.lkc.yupao.common;

/**
 * @author lkc
 * @version 1.0
 * 返回工具类
 */
public class ResultUtils {
    /**
     * 成功
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * 失败
     * @param errorCode
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    /**
     *
     * @param errorCode
     * @param message
     * @param description
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode,String message,String description){
        return new BaseResponse<>(errorCode,message,description);
    }

    /**
     *
     * @param errorCode
     * @param message
     * @param description
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> error(int errorCode,String message,String description){
        return new BaseResponse<>(errorCode,message,description);
    }

    /**
     *
     * @param errorCode
     * @param description
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode,String description){
        return new BaseResponse<>(errorCode,description);
    }
}
