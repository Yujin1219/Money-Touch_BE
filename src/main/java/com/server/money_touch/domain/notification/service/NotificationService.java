package com.server.money_touch.domain.notification.service;

import com.server.money_touch.domain.notification.dto.NotificationResponse;

public interface NotificationService {

    /**
     * 커서 기반 무한스크롤로 알림 목록 조회
     * @param userId 사용자 ID
     * @param cursorId 커서 ID (첫 조회시 null)
     * @return 알림 목록 DTO
     */
    NotificationResponse.NotificationListDTO getNotificationsByCursor(Long userId, Long cursorId);

    /**
     * 알림 읽음 처리
     * @param userId 사용자 ID
     * @param notificationId 알림 ID
     * @return 읽음 처리 결과 DTO
     */
    NotificationResponse.NotificationReadResultDTO markAsRead(Long userId, Long notificationId);
}
