package com.server.money_touch.notification.entity;


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

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(nullable = false, length = 50)
    private String content;

    private Boolean isRead = false;

    @Column(nullable = false)
    private Long targetId;
}
