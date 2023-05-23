package com.lkc.yupao.reids;
import java.util.Set;

import com.lkc.yupao.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author lkc
 * @version 1.0
 * Spring Data:通用的数据访问框架，定义了一组增删改查的接口
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    //操作redis的字符串
    @Test
    public void test01(){
        //操作字符串
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //设置值
        valueOperations.set("lkc1","1");
        valueOperations.set("lkc2","12");
        valueOperations.set("lkc3","123");
        valueOperations.set("lkc4",1);
        User user = new User();
        user.setUsername("111111");
        valueOperations.set("lkc5",user);
        //获取值
        Object lkc1 = valueOperations.get("lkc1");
        Assertions.assertTrue("1".equals((String)lkc1));
    }

    //删除所有key
    @Test
    public void test02() {
        // 获取所有的key
        Set<String> keys = redisTemplate.keys("*");
        // 如果存在key，则逐个删除
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
