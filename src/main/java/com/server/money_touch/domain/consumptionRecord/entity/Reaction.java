package com.server.money_touch.domain.consumptionRecord.entity;

import com.server.money_touch.domain.consumptionRecord.enums.ReactionType;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Reaction extends BaseEntity {

    // 유저 관계 매핑

    // 소비기록 관계 매핑

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

}

