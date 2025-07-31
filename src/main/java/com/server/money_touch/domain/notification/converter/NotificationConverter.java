package com.server.money_touch.domain.notification.converter;

import com.server.money_touch.domain.notification.dto.NotificationResponse;
import com.server.money_touch.domain.notification.entity.Notification;
import org.springframework.data.domain.Slice;

import java.util.List;

public class NotificationConverter {

    // Notification 엔터티 -> NotificationDetailDTO
    public static NotificationResponse.NotificationDetailDTO toNotificationDetailDTO(Notification notification) {

        if(notification == null) {
            return null;
        }

        // 게시글 기반 알림 타입이면 imageUrl 포함
        String typeName = notification.getNotificationType().getNotificationTypeName();
        boolean isPostRelated = typeName.equals("COMMENT")
                || typeName.equals("WISE")
                || typeName.equals("WASTE");

        return NotificationResponse.NotificationDetailDTO.builder()
                .notificationId(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .notificationTypeName(notification.getNotificationType().getNotificationTypeName())
                .imgUrl(notification.getNotificationType().getImgUrl())
                .senderName(notification.getSenderName())
                .targetId(notification.getTargetId())
                .isRead(notification.getIsRead())
                .imageUrl(isPostRelated ? notification.getImageUrl() : null)
                .createdAt(notification.getCreatedAt())
                .build();

    }

    // 무한스크롤용 알림 목록 DTO 변환
    public static NotificationResponse.NotificationListDTO toNotificationListDTO(Slice<Notification> notificationSlice) {

        if (notificationSlice == null) {
            return NotificationResponse.NotificationListDTO.builder()
                    .notificationList(List.of())
                    .notificationListSize(0)
                    .isFirst(true)
                    .hasNext(false)
                    .nextCursorId(null)
                    .build();
        }

        List<NotificationResponse.NotificationDetailDTO> detailDTOList = notificationSlice.getContent().stream()
                .map(NotificationConverter::toNotificationDetailDTO)
                .toList();

        // 다음 커서 ID 설정 (마지막 아이템의 ID)
        Long nextCursorId = null;
        if (notificationSlice.hasNext() && !detailDTOList.isEmpty()) {
            nextCursorId = detailDTOList.get(detailDTOList.size() - 1).getNotificationId();
        }

        return NotificationResponse.NotificationListDTO.builder()
                .notificationList(detailDTOList)
                .notificationListSize(detailDTOList.size())
                .isFirst(notificationSlice.isFirst())
                .hasNext(notificationSlice.hasNext())
                .nextCursorId(nextCursorId)
                .build();
    }
}
