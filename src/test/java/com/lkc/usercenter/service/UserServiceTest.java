package com.lkc.usercenter.service;
import java.util.Date;

import com.lkc.usercenter.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author lkc
 * @version 1.0
 * 用户服务测试1
 */
@SpringBootTest
class UserServiceTest {
    @Resource
    private UserService userService;

//    @Test
//    public void testAddUser(){
//        User user = new User();
//        user.setUsername("lkc");
//        user.setUserAccount("11111111");
//        user.setAvatarUrl("");
//        user.setGender(0);
//        user.setUserPassword("123");
//        user.setEmail("123");
//        user.setPhone("123");
//        user.setCreateTime(new Date());
//        user.setUpdateTime(new Date());
//        boolean save = userService.save(user);
//        assertTrue(save);
//    }

//    @Test
//    void userRegister() {
//        String userAccount="lkc666";
//        String userPassword="123456";
//        String checkPassword="123456";
//        Long result = userService.userRegister(userAccount, userPassword, checkPassword, );
//        assertEquals(-1,result);
//        userAccount="uu";
//        result=userService.userRegister(userAccount,userPassword,checkPassword, );
//        assertEquals(-1,result);
//        userAccount="uu uuu";
//        result=userService.userRegister(userAccount,userPassword,checkPassword, );
//        assertEquals(-1,result);
//        userAccount="lkc6666";
//        userPassword="123456789";
//        checkPassword="123456789";
//        result=userService.userRegister(userAccount,userPassword,checkPassword, );
//        assertEquals(-1,result);
//    }
}