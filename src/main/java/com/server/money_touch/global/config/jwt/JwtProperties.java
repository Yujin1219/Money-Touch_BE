package com.server.money_touch.global.config.jwt;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@Slf4j
@ConfigurationProperties("jwt")
public class JwtProperties {
    private String secretKey = "";
    private Expiration expiration;

    @Getter
    @Setter
    public static class Expiration {
        private long access;
        private long refresh;
    }
    // ✅ 확인용
    @PostConstruct
    public void init() {
        log.info("✅ JWT Secret Key = " + secretKey);
        log.info("✅ Access Token Expiration = " + expiration.getAccess());
        log.info("✅ Refresh Token Expiration = " + expiration.getRefresh());
    }
}
