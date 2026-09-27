package com.gec.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 业务缓存封装（Redis）
 *
 * 项目早就引入了 spring-boot-starter-data-redis，但只有一个配置类，业务里从来没用过。
 * 这里补一层薄封装，给读多写少的 C 端接口用。
 *
 * 两条硬要求：
 *   1. 所有操作都 try-catch 降级 —— Redis 挂了绝不能影响主流程，
 *      缓存拿不到就回源数据库，写不进去就跳过。
 *   2. 开关 cache.enabled=false 可以整体关掉。
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Value("${cache.enabled:true}")
    private boolean enabled;

    @Value("${cache.ttl-seconds:300}")
    private long ttlSeconds;

    private final ObjectMapper mapper = new ObjectMapper();

    private boolean usable() {
        return enabled && redisTemplate != null;
    }

    /** 取缓存，反序列化成指定类型；没有或出错都返回 null */
    public <T> T get(String key, Class<T> type) {
        if (!usable()) {
            return null;
        }
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return mapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("缓存读取失败，回源数据库 key={} msg={}", key, e.getMessage());
            return null;
        }
    }

    /** 取缓存的原始 JSON 字符串（给需要直接往外吐的接口用） */
    public String getRaw(String key) {
        if (!usable()) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("缓存读取失败 key={} msg={}", key, e.getMessage());
            return null;
        }
    }

    public void set(String key, Object value) {
        if (!usable() || value == null) {
            return;
        }
        try {
            String json = mapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("缓存写入失败（忽略） key={} msg={}", key, e.getMessage());
        }
    }

    public void delete(String key) {
        if (!usable()) {
            return;
        }
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("缓存删除失败（忽略） key={} msg={}", key, e.getMessage());
        }
    }

    /** 按前缀批量清（商品改动后把 shop: 开头的缓存全清掉） */
    public void deleteByPrefix(String prefix) {
        if (!usable()) {
            return;
        }
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("缓存前缀清理失败（忽略） prefix={} msg={}", prefix, e.getMessage());
        }
    }
}
