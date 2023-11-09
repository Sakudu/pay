package org.example.pay.util;

import cn.hutool.core.util.StrUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author gwd
 * @date 2023/11/9 16:34
 */
@Component
public class RedisUtil {

    @Resource
    private StringRedisTemplate redisTemplate;

    public void addZSet(String key, String value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    public boolean getLock(String key, String value, Long time, TimeUnit timeUnit) {
        ValueOperations<String, String> operation = redisTemplate.opsForValue();
        return Boolean.TRUE.equals(operation.setIfAbsent(key, value, time, timeUnit));
    }

    public void releaseLock(String key, String value) {
        String currentValue = redisTemplate.opsForValue().get(key);
        if (!StrUtil.isEmpty(currentValue) && currentValue.equals(value)) {
            redisTemplate.opsForValue().getOperations().delete(key);
        }
    }
}
