package com.lkc.yupao.reids;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author lkc
 * @version 1.0
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void test01(){
        //list 数据存在本地 JVM内存中
        List<String> list = new ArrayList<>();
        list.add("lkc");
        System.out.println(list.get(0));


        //存在redis内存中
        RList<Object> list1 = redissonClient.getList("test-list");
//        list1.add("ooo");
        System.out.println(list1.get(0));
        list1.remove(0);

    }

    /**
     * 测试看门口机制
     * 原理：
     *  1.监听当前线程，默认过期时间是30秒，每10秒续期一次（补到30秒）
     *  2.如果线程挂掉（注意debug模式也会被它当成服务器宕机），则不会续期
     */
    @Test
    public void test02(){
        //获取分布式锁的实例
        RLock lock = redissonClient.getLock("yupao:prechachejob:recommend:lock");
        try {
            //只有一个线程能获取锁
            //写锁  l:等待时间(等待时间0，拿到锁就执行，拿不到就走)  l1:锁的过期时间（锁到达时间就过期）-1表示过期
            if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)) {
//                Thread.sleep(3000000);
                //获取当前线程的id
                System.out.println(Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
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
