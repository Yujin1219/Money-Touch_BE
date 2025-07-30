package com.server.money_touch.domain.notification.entity;


import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification extends BaseEntity {

    // 알림 수신자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 알림 유형
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_type_id", nullable = false)
    private NotificationType notificationType;

    // 알림 발신자 이름
    private String senderName;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(nullable = false, length = 70)
    private String content;

    private Boolean isRead = false;

    @Column(nullable = false)
    private Long targetId;

    // 게시글에 관한 알림일 경우 게시글 사진
    private String imageUrl;

    // 읽음 처리
    public void markAsRead() {
        this.isRead = true;
    }
}
