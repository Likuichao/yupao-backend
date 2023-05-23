package com.lkc.yupao.service;

import com.lkc.yupao.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

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
    public void testSearchUserByTags(){
        List<String> list = Arrays.asList("java", "php");
        List<User> users = userService.searchUserByTags(list);
        System.out.println(users);
        Assertions.assertNotNull(users);
    }
}