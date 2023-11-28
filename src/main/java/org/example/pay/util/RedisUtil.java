package org.example.pay.util;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author gwd
 * @date 2023/11/9 16:34
 */
@Component
public class RedisUtil {

    @Autowired
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

    public boolean zSetIsEmpty(String key) {
        Long size = redisTemplate.opsForZSet().zCard(key);
        return size == null || size <= 0L;
    }

    public String getZSetValue(String key) {
        Set<String> range = redisTemplate.opsForZSet().range(key, 0, 0);
        if (range != null && !range.isEmpty()) {
            String next = range.iterator().next();
            deleteZSetValue(key, next);
            return next;
        }
        return null;
    }

    public void deleteZSetValue(String key, String value){
        redisTemplate.opsForZSet().remove(key, value);
    }
}
