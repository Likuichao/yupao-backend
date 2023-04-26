package com.lkc.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lkc
 * @version 1.0
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 2692852445782542405L;

    private String userAccount;

    private String userPassword;
}
