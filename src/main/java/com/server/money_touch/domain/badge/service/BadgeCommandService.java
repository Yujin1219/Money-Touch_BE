package com.server.money_touch.domain.badge.service;

import com.server.money_touch.domain.badge.dto.BadgeResponse;

public interface BadgeCommandService {

    // 대표 배지 설정
    BadgeResponse.RepresentativeBadgeResultDTO setRepresentativeBadge(Long userId, Long badgeId);
}
