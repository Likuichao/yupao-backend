package com.lkc.yupao.once;

import com.lkc.yupao.mapper.UserMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @author lkc
 * @version 1.0
 */
@Component
public class InsertUsers {
    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户
     */
    //@Scheduled注解是spring boot提供的用于定时任务控制的注解,主要用于控制任务在某个指定时间执行,或者每隔一段时间执行
    // 注意需要配合@EnableScheduling使用
    // 上一次执行完毕时间点之后5秒再执行  fixedDelay =5000
//    @Scheduled(fixedRate = Long.MAX_VALUE)
    public void doInsertUsers() {
        //计算代码耗时
        StopWatch stopWatch = new StopWatch();
//        System.out.println("1111");
        stopWatch.start();

//        final int INSERT_NUM = 10000000;
//        for (int i = 0; i < INSERT_NUM; i++) {
//            User user = new User();
//            user.setUsername("假lkc");
//            user.setUserAccount("fake123");
//            user.setAvatarUrl("");
//            user.setGender(0);
//            user.setUserPassword("fake123");
//            user.setEmail("fake123");
//            user.setUserStatus(0);
//            user.setPhone("fake123");
//            user.setCreateTime(new Date());
//            user.setUpdateTime(new Date());
//            user.setUserRole(0);
//            user.setPlanetCode("11");
//            userMapper.insert(user);
//        }

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }


}
