package com.server.money_touch.domain.consumptionRecord.entity;

import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class TotalConsumption extends BaseEntity {
    @Column(columnDefinition = "INT DEFAULT 0", nullable = false)
    private Integer totalConsumptionAmount; // 총 소비 금액

    // 총 소비-유저 다대일 연관관계
}
