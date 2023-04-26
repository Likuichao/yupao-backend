package com.lkc.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 * @author lkc
 * @version 1.0
 */
@Data
public class UserResisterRequest implements Serializable {

    private static final long serialVersionUID = 6492161035859601873L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String planetCode;
}
