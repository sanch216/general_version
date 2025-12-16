package com.trinity.courierapp.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisCache {

    @Autowired
    private RedisTemplate<String, Object> redis;

    public void save(String key, Object value, long ttlSeconds) {
        redis.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    public <T> T get(String key, Class<T> type) {
        Object val = redis.opsForValue().get(key);
        return type.cast(val);
    }

    public void delete(String key) {
        redis.delete(key);
    }

}
