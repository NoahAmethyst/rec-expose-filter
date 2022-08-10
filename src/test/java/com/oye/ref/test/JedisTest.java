package com.oye.ref.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
public class JedisTest {


    @Test
    public void test() {
        log.info("{}", false && false);
    }

    @Test
    public void setValue() {
        JedisPool jedisPool = new JedisPool("10.252.197.228", 6379);

        Jedis jedis = jedisPool.getResource();
        jedis.select(0);
        jedis.set("jedisTest", "test");
        System.out.println(jedis.get("jedisTest"));
        jedis.close();
        jedisPool.close();
    }
}
