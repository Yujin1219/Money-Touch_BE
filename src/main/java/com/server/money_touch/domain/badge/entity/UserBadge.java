package com.server.money_touch.domain.badge.entity;

import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserBadge extends BaseEntity {

    // 유저 관계 매핑

    // 배지 관계 매핑

}