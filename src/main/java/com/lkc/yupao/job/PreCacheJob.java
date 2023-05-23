package com.lkc.yupao.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lkc.yupao.model.domain.User;
import com.lkc.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 缓存预热任务
 * @author lkc
 * @version 1.0
 */
@Component
@Slf4j
public class PreCacheJob {
    /**1.开启定时任务，根据cron表达式的时间，去更新redis缓存
     *      1．主类开启EnableScheduling
     *      2．给要定时执行的方法添加@Scheduling 注解，指定cron表达式或者执行频率
     *      cron生成器:https://cron.qqe2.com/
     * 2.使用redission实现分布式锁，保定定时任务不重复执行
     */
    @Resource
    private RedissonClient redissonClient; //创建分布式锁实例

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;//spring data redis操作redis

    private List<Long> mainUserList = Arrays.asList(1L);//重点用户

    //每天执行，预热推荐用户 只需要一台服务器执行预热即可
    @Scheduled(cron = "0 59 23 * * ? ") //秒 分 时 日 月 年
    public void doCacheRecommendUser(){
        //获取分布式锁的实例
        RLock lock = redissonClient.getLock("yupao:prechachejob:recommend:lock");
        try {
            //只有一个线程能获取锁
            //写锁  l:等待时间(等待时间0，拿到锁就执行，拿不到就走)  l1:锁的过期时间（锁到达时间就过期）
            if (lock.tryLock(0,30000, TimeUnit.MILLISECONDS)) {
                //获取当前线程的id
                System.out.println(Thread.currentThread().getId());
                //更新redis缓存
                for (Long userId : mainUserList) {
                    //查询数据库
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    //用户信息脱敏
                    List<User> list = userPage.getRecords().stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
                    userPage.setRecords(list);
                    //在缓存中保存id（操作redis的string）
                    String redisKey = String.format("yupao:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    try {
                        valueOperations.set(redisKey,userPage,24, TimeUnit.HOURS);//同时设置过期时间为24h  可能出现缓存雪崩！！
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放锁要放在finally，防止代码出现异常
            //只能释放自己的锁
            if(lock.isHeldByCurrentThread()){
                //获取当前线程的id
                System.out.println(Thread.currentThread().getId());
                //释放锁
                lock.unlock();
            }
        }

    }
}
