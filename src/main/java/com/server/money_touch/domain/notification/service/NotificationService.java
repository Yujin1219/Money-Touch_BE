package com.server.money_touch.domain.notification.service;

import com.server.money_touch.domain.notification.dto.NotificationResponse;

public interface NotificationService {

    /**
     * 커서 기반 무한스크롤로 알림 목록 조회
     * @param userId 사용자 ID
     * @param cursorId 커서 ID (첫 조회시 null)
     * @param size 페이지 사이즈
     * @return 알림 목록 DTO
     */
    NotificationResponse.NotificationListDTO getNotificationsByCursor(Long userId, Long cursorId, int size);

}
