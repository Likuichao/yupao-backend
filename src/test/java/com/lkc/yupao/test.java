package com.lkc.yupao;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

/**远程连接redis
 * @author lkc
 * @version 1.0
 */
public class test {

    @Test
    public void test01() {
        Jedis jedis = new Jedis("8.130.93.91", 6379);
        jedis.auth("opo15639");  //你自己设置的密码，如果没有密码，则不需要当前那行
        jedis.set("K1","V1");
        System.out.println(jedis.ping());
    }
}
