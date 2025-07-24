package com.server.money_touch.domain.badge.repository;

import com.server.money_touch.domain.badge.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    // 배지 존재 여부 확인
    boolean existsById(Long id);

}
