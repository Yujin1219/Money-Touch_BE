package com.server.money_touch.domain.badge.repository;

import com.server.money_touch.domain.badge.entity.UserBadge;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    // 사용자의 모든 배지 조회
    List<UserBadge> findByUser(User user);

    // 사용자가 특정 배지를 획득하였는지 확인 (사용자와 배지 ID로 UserBadge 조회)
    Optional<UserBadge> findByUserAndBadgeId(User user, Long badgeId);


}
