package com.server.money_touch.domain.notification.service;

import com.server.money_touch.domain.notification.converter.NotificationConverter;
import com.server.money_touch.domain.notification.dto.NotificationResponse;
import com.server.money_touch.domain.notification.entity.Notification;
import com.server.money_touch.domain.notification.repository.NotificationRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 커서 기반 무한스크롤로 알림 목록 조회
     */
    public NotificationResponse.NotificationListDTO getNotificationsByCursor(
            Long userId, Long cursorId, int size) {

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, size);
        Slice<Notification> notificationSlice;

        // 첫 페이지인지 확인 (cursorId가 null이면 첫 페이지)
        if (cursorId == null) {
            notificationSlice = notificationRepository.findFirstPageNotifications(userId, pageable);
        } else {
            notificationSlice = notificationRepository.findNotificationsByCursor(userId, cursorId, pageable);
        }

        // Converter 사용하여 DTO 변환
        return NotificationConverter.toNotificationListDTO(notificationSlice);

    }
}
