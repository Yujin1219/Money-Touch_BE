package com.server.money_touch.global.test;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 직접 명령(RedisTemplate) + Spring Cache(캐시 매니저) 모두 테스트 가능한 컨트롤러
 * - /redis/set|get|exists|delete|ttl|expire: Redis 키-값 직접 확인
 * - /redis/cache/*: Spring Cache 추상화를 통해 RedisCacheManager로 캐시 동작 확인
 */
@Tag(name = "redis-test-controller", description = "Redis Test(관리자용)")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/redis")
public class RedisTestController {

    // Redis 문자열 명령을 직접 테스트하기 위한 템플릿
    private final RedisTemplate<String, String> redisTemplate;

    // Spring Cache 추상화를 통한 동적 캐시 접근 (백엔드는 RedisCacheManager)
    private final CacheManager cacheManager;

    // -------------------- RedisTemplate 직접 테스트 --------------------

    /**
     * set key value [expireSeconds]
     * expireSeconds가 지정되면 TTL과 함께 저장한다.
     */
    @PostMapping("/set")
    public ResponseEntity<?> setValue(@RequestParam String key,
                                      @RequestParam String value,
                                      @RequestParam(required = false) Long expireSeconds) {
        try {
            if (expireSeconds != null) {
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(expireSeconds));
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return ResponseEntity.ok(Map.of("status", "success", "key", key, "value", value));
        } catch (Exception e) {
            log.error("Redis SET 오류", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * get key
     */
    @GetMapping("/get")
    public ResponseEntity<?> getValue(@RequestParam String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Key not found"));
            }
            return ResponseEntity.ok(Map.of("key", key, "value", value));
        } catch (Exception e) {
            log.error("Redis GET 오류", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * EXISTS key
     */
    @GetMapping("/exists")
    public ResponseEntity<?> checkExistence(@RequestParam String key) {
        try {
            boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
            return ResponseEntity.ok(Map.of("key", key, "exists", exists));
        } catch (Exception e) {
            log.error("Redis EXISTS 오류", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DEL key
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteKey(@RequestParam String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            return ResponseEntity.ok(Map.of("key", key, "deleted", deleted));
        } catch (Exception e) {
            log.error("Redis DELETE 오류", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * TTL key (초 단위)
     * -2: 키 없음, -1: TTL 설정 없음
     */
    @GetMapping("/ttl")
    public ResponseEntity<?> getTTL(@RequestParam String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl == null) {
                return ResponseEntity.ok(Map.of("key", key, "ttl", "Unknown"));
            }
            if (ttl == -2) {
                return ResponseEntity.status(404).body(Map.of("key", key, "ttl", "Key not found"));
            }
            if (ttl == -1) {
                return ResponseEntity.ok(Map.of("key", key, "ttl", "No expiration"));
            }
            return ResponseEntity.ok(Map.of("key", key, "ttl_seconds", ttl));
        } catch (Exception e) {
            log.error("Redis TTL 오류", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * EXPIRE key seconds
     */
    @PostMapping("/expire")
    public ResponseEntity<?> expireKey(@RequestParam String key, @RequestParam Long seconds) {
        try {
            Boolean result = redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
            return ResponseEntity.ok(Map.of("key", key, "expireSet", result));
        } catch (Exception e) {
            log.error("Redis EXPIRE 오류", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // -------------------- Spring Cache(백엔드: Redis) 테스트 --------------------

    /**
     * 캐시에 값 조회. 미스일 경우 계산 후 캐시에 저장한다.
     * cacheName은 동적으로 지정 가능하며, RedisCacheManager가 해당 이름의 캐시를 생성/사용한다.
     * TTL/직렬화 등은 RedisCacheConfig에서 정의한 정책이 적용된다.
     */
    @GetMapping("/cache/get")
    public ResponseEntity<?> getCachedValue(@RequestParam String cacheName,
                                            @RequestParam String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown cache: " + cacheName));
        }

        Cache.ValueWrapper hit = cache.get(key);
        if (hit != null) {
            return ResponseEntity.ok(Map.of("cache", cacheName, "key", key, "value", hit.get(), "hit", true));
        }

        // 캐시 미스: 계산 후 put
        String computed = "ComputedValueFor_" + key;
        cache.put(key, computed);
        log.info("캐시 미스 -> 저장, cacheName={}, key={}", cacheName, key);
        return ResponseEntity.ok(Map.of("cache", cacheName, "key", key, "value", computed, "hit", false));
    }

    /**
     * 캐시 값 강제 갱신(put).
     * 반환값이 아니라 cache.put을 직접 호출해 즉시 캐시를 덮어쓴다.
     */
    @PostMapping("/cache/update")
    public ResponseEntity<?> updateCachedValue(@RequestParam String cacheName,
                                               @RequestParam String key,
                                               @RequestParam String value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown cache: " + cacheName));
        }
        cache.put(key, value);
        log.info("캐시 업데이트, cacheName={}, key={}, value={}", cacheName, key, value);
        return ResponseEntity.ok(Map.of("cache", cacheName, "key", key, "updatedValue", value));
    }

    /**
     * 캐시 엔트리 제거(evict).
     * 특정 key만 제거한다.
     */
    @DeleteMapping("/cache/evict")
    public ResponseEntity<?> evictCache(@RequestParam String cacheName,
                                        @RequestParam String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown cache: " + cacheName));
        }
        cache.evict(key);
        log.info("캐시 삭제, cacheName={}, key={}", cacheName, key);
        return ResponseEntity.ok(Map.of("cache", cacheName, "key", key, "cacheEvicted", true));
    }

    /**
     * 캐시 전체 비우기(clear).
     * 주의: 해당 cacheName의 모든 엔트리가 삭제된다.
     */
    @DeleteMapping("/cache/clear")
    public ResponseEntity<?> clearCache(@RequestParam String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown cache: " + cacheName));
        }
        cache.clear();
        log.info("캐시 전체 삭제, cacheName={}", cacheName);
        return ResponseEntity.ok(Map.of("cache", cacheName, "cleared", true));
    }
}
