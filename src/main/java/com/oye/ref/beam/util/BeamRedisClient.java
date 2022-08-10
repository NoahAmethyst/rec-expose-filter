package com.oye.ref.beam.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BeamRedisClient<T> {

    private static JedisPool jedisPool;

    private String redisHost;

    private int redisPort;

    public BeamRedisClient(String redisHost, int redisPort) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }


    public void set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(getRedisHost(), getRedisPort()).getResource();
            jedis.set(key, value);
        } catch (Exception e) {
            log.error("set key {} to redis error:{}", key, e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void clearElements(String key) {
        delKey(key);
    }

    public void delKey(String key) {
        Jedis jedis = null;

        try {
            jedis = getJedisPool(getRedisHost(), getRedisPort()).getResource();
            jedis.del(key);
        } catch (Exception e) {
            log.error("clear key {} from redis error:{}", key, e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void addElementsToList(String key, Object value) {
        Jedis jedis = null;

        try {
            jedis = getJedisPool(getRedisHost(), getRedisPort()).getResource();
            String element = JSONObject.toJSONString(value);
            jedis.rpush(key, element);
            jedis.expire(key, 3 * 24 * 60 * 60);
        } catch (Exception e) {
            log.error("save element type:{} to redis error:{}", e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public List<T> getElements(String key, Class<T> clazz) {
        Jedis jedis = null;
        List<T> elements = new ArrayList();
        try {
            jedis = getJedisPool(getRedisHost(), getRedisPort()).getResource();
            List<String> actionStringList = jedis.lrange(key, 0, -1);
            actionStringList.forEach(string -> {
                elements.add(JSONObject.parseObject(string, clazz));
            });
        } catch (Exception e) {
            log.error("get elements type:{} from redis error:{}", clazz.getName(), e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
            return elements;
        }
    }

    private JedisPool getJedisPool(String host, int port) {
        if (jedisPool == null) {
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            jedisPool = new JedisPool(poolConfig, host, port);
        }
        return jedisPool;
    }
}
