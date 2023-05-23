package com.lkc.yupao.SQLquery;

import com.lkc.yupao.mapper.UserMapper;
import com.lkc.yupao.model.domain.User;
import com.lkc.yupao.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author lkc
 * @version 1.0
 * 导入用户数据
 */
@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    //建立执行器（线程池）
    // 参数：核心线程数量（默认始终线程运行） 最大线程数量  存活时间（when the number of threads is greater than the core, this is the maximum）
    // 参数：时间单位（此处为分钟）  任务队列（当任务队列满了，就会分配更多的线程，也就是大于60，分配更多的线程）
    // 参数：RejectedExecutionHandler handler（当加了线程之后，任务队列还是满的，干不完，默认就会拒绝新来的任务）
    private ExecutorService executorService = new ThreadPoolExecutor(60,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));

    //连接池参数设置
    //CPU密集型：分配的线程数=CPU-1 （任务都在计算，在内存中做运算比如+-*/；cpu运行主线程）
    //IO密集型： 分配核心线程数可以大于CPU核数 （IO耗时：网络传输，操作数据库，缓存，消息队列，写磁盘等；CPU不干活，CPU做插入，执行插入的是数据库，不是当前这台机器）

    /**
     * 一个个插入用户 userMapper.insert(user);
     */

    public void doInsertUsers01() {
        //计算代码耗时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假lkc");
            user.setUserAccount("fake123");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword("fake123");
            user.setEmail("fake123");
            user.setUserStatus(0);
            user.setPhone("fake123");
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            user.setUserRole(0);
            user.setPlanetCode("11");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 批量插入用户 userService.saveBatch
     */

    public void doInsertUsers02() {
        //计算代码耗时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假lkc");
            user.setUserAccount("fake123");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword("fake123");
            user.setEmail("fake123");
            user.setUserStatus(0);
            user.setPhone("fake123");
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            user.setUserRole(0);
            user.setPlanetCode("11");
//            userMapper.insert(user);
            userList.add(user);
        }
        //20s 10w数据
        userService.saveBatch(userList, 100);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
    /**
     * 并发插入用户
     */

    public void doConcurrentInsertUsers() {
        //计算代码耗时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        int batchSize=2500;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        //分10组,当分组20时吗，时间反而增加了
        int j=0;
        for (int i = 0; i < 40; i++) {
            //使用并发时不要使用非并发的集合
            List<User> userList = Collections.synchronizedList(new ArrayList<>());
            while(true) {
                j++;
                User user = new User();
                user.setUsername("假lkc");
                user.setUserAccount("fake123");
                user.setAvatarUrl("");
                user.setGender(0);
                user.setUserPassword("fake123");
                user.setEmail("fake123");
                user.setUserStatus(0);
                user.setPhone("fake123");
                user.setCreateTime(new Date());
                user.setUpdateTime(new Date());
                user.setUserRole(0);
                user.setPlanetCode("11");
                userList.add(user);
                if(j%batchSize == 0){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName="+Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            },executorService);//使用自己定义的执行器（线程池）；原来默认使用的ForkJoinPool线程池存在的问题：有的线程没有干活，有的线程干多个任务
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
