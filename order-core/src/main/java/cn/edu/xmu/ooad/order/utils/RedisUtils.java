package cn.edu.xmu.ooad.order.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具
 *
 * @author Han Li
 * Created at 4/12/2020 12:17 上午
 * Modified by Han Li at 4/12/2020 12:17 上午
 */
@Component
public class RedisUtils {

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;
    @Autowired
    private ReactiveRedisTemplate<String, Serializable> reactiveRedisTemplate;

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个 key
     */
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(Arrays.asList(key));
            }
        }
    }

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public <T extends Serializable> T get(String key, Class<T> tClass) {
        if (key == null) {
            return null;
        }
        Serializable obj;
        try {
            obj = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Redis 错误：" + e.getMessage());
            return null;
        }
        return tClass.cast(obj);
    }

    /**
     * 响应式缓存获取
     *
     * @param key 键
     * @return 值
     */
    public <T extends Serializable> Mono<T> reactiveGet(String key, Class<T> tClass) {
        if (key == null) {
            return null;
        }
        Mono<Serializable> obj;
        try {
            obj = reactiveRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Redis 错误：" + e.getMessage());
            return null;
        }
        return obj.map(x -> tClass.cast(obj));
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Serializable value, long timeout) {
        if (timeout <= 0) {
            timeout = 60;
        }
        try {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 响应式缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public Mono<Boolean> reactiveSet(String key, Serializable value, long timeout) {
        if (timeout <= 0) {
            timeout = 60;
        }
        try {
            return reactiveRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeout));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     */
    public long dec(String key, long delta) {
        if (delta > 0) {
            return redisTemplate.opsForValue().increment(key, -delta);
        } else {
            return 0;
        }
    }

    /**
     * 执行 LUA 脚本
     * @param script
     * @param args
     * @param keys
     * @param <T>
     * @return
     */
    public Object execute(String script, List<String> keys, Object ...args) {
        DefaultRedisScript<Object> redisScript = new DefaultRedisScript<>(script);
        Object res;
        try {
            res = redisTemplate.execute(redisScript, keys, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return res;
    }
}
