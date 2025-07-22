package com.server.money_touch.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자 알림 목록")
    public static class NotificationListDTO {
        @Schema(description = "사용자의 알림 목록")
        List<NotificationDetailDTO> notificationList;

        @Schema(description = "현재 페이지의 알림 개수", example = "10")
        Integer notificationListSize;

        @Schema(description = "첫 페이지 여부", example = "true")
        private boolean isFirst;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private boolean isLast;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private boolean hasNext;

        @Schema(description = "다음 커서 ID (무한스크롤용)", example = "123")
        private Long nextCursorId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자 알림 세부 정보")
    public static class NotificationDetailDTO {

        @Schema(description = "알림 아이디", example = "1")
        private Long notificationId;

        @Schema(description = "알림 제목", example = "새로운 댓글")
        private String title;

        @Schema(description = "알림 내용", example = "회원님의 게시물에 새로운 댓글이 달렸습니다.")
        private String content;

        @Schema(description = "알림 유형 이름", example = "댓글")
        private String notificationTypeName;

        @Schema(description = "알림 유형 이미지 URL", example = "http://example.com/comment.png")
        private String imgUrl;

        @Schema(description = "발신자 아이디", example = "2")
        private Long senderId;

        @Schema(description = "대상 아이디", example = "10")
        private Long targetId;

        @Schema(description = "읽음 여부", example = "false")
        private Boolean isRead;

        @Schema(description = "알림 생성 일시", example = "2025-01-15T14:30:25")
        private LocalDateTime createdAt;

    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "알림 읽음 처리 결과 DTO")
    public static class NotificationReadResultDTO {

        @Schema(description = "알림 ID", example = "1")
        private Long notificationId;

        @Schema(description = "읽음 처리 여부", example = "true")
        private boolean isRead;

        @Schema(description = "처리 메시지", example = "알림이 읽음 처리되었습니다.")
        private String message;

    }

}
