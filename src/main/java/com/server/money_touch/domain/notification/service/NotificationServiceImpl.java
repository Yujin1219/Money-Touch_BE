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

    private static final Integer PAGE_SIZE = 10;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 커서 기반 무한스크롤로 알림 목록 조회
     */
    @Override
    public NotificationResponse.NotificationListDTO getNotificationsByCursor(
            Long userId, Long cursorId) {

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, PAGE_SIZE);

        // 하나의 메서드로 커서 기반 조회 (cursorId가 null이면 첫 페이지)
        Slice<Notification> notificationSlice =
                notificationRepository.findNotificationsByCursor(userId, cursorId, pageable);

        // Converter 사용하여 DTO 변환
        return NotificationConverter.toNotificationListDTO(notificationSlice);

    }

    /**
     * 알림 읽음 처리
     */
    @Override
    @Transactional
    public NotificationResponse.NotificationReadResultDTO markAsRead(Long userId, Long notificationId) {

        // 존재하는 알림인지 확인
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.NOTIFICATION_NOT_FOUND));

        // 해당 사용자의 알림인지 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new ErrorHandler(ErrorStatus.NO_PERMISSION_FOR_NOTIFICATION);
        }

        // 이미 읽은 알림인지 확인
        if (notification.getIsRead()) {
            throw new ErrorHandler(ErrorStatus.ALREADY_READ_NOTIFICATION);
        }

        // 알림 읽음 처리
        notification.markAsRead();

        return NotificationResponse.NotificationReadResultDTO.builder()
                .notificationId(notificationId)
                .isRead(true)
                .message("알림이 읽음 처리되었습니다.")
                .build();
    }

}
