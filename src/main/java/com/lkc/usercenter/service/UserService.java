package com.lkc.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lkc.usercenter.model.User;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 * @author 19733
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2023-04-10 21:12:50
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @param planetCode
     * @return
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 退出登录
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);
}
