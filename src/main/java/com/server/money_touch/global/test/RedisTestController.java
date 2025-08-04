package com.server.money_touch.global.test;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Tag(name = "redis-test-controller", description = "Redis Test(관리자용)")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/redis")
public class RedisTestController {

    private final RedisTemplate<String, String> redisTemplate;

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
            log.error("Redis SET 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getValue(@RequestParam String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Key not found"));
            }
            return ResponseEntity.ok(Map.of("key", key, "value", value));
        } catch (Exception e) {
            log.error("Redis GET 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<?> checkExistence(@RequestParam String key) {
        try {
            boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
            return ResponseEntity.ok(Map.of("key", key, "exists", exists));
        } catch (Exception e) {
            log.error("Redis EXISTS 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteKey(@RequestParam String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            return ResponseEntity.ok(Map.of("key", key, "deleted", deleted));
        } catch (Exception e) {
            log.error("Redis DELETE 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/ttl")
    public ResponseEntity<?> getTTL(@RequestParam String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl == null || ttl < 0) {
                return ResponseEntity.ok(Map.of("key", key, "ttl", "No expiration or key not found"));
            }
            return ResponseEntity.ok(Map.of("key", key, "ttl_seconds", ttl));
        } catch (Exception e) {
            log.error("Redis TTL 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/expire")
    public ResponseEntity<?> expireKey(@RequestParam String key, @RequestParam Long seconds) {
        try {
            Boolean result = redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
            return ResponseEntity.ok(Map.of("key", key, "expireSet", result));
        } catch (Exception e) {
            log.error("Redis EXPIRE 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}