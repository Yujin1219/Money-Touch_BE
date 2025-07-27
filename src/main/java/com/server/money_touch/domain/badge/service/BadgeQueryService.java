package com.server.money_touch.domain.badge.service;

import com.server.money_touch.domain.badge.dto.BadgeResponse;

public interface BadgeQueryService {

    // 사용자가 획득한 배지 목록 조회
    BadgeResponse.MyBadgeListResultDTO getMyBadges(Long userId);

    // 현재 대표 배지 조회
    BadgeResponse.RepresentativeBadgeResultDTO getRepresentativeBadge(Long userId);
}
