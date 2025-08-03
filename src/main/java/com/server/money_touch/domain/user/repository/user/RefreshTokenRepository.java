package com.server.money_touch.domain.user.repository.user;

import com.server.money_touch.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByEmail(String email);
    void deleteByEmail(String email); // 중복 저장 방지를 위한 삭제 메서드
}
