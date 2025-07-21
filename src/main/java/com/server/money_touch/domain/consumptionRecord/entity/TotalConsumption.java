package com.server.money_touch.domain.consumptionRecord.entity;

import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_created_month",
                        columnNames = {"user_id", "created_month"}
                )
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class TotalConsumption extends BaseEntity {
    @Column(columnDefinition = "INT DEFAULT 0", nullable = false)
    @Builder.Default
    private Integer totalConsumptionAmount = 0; // 총 소비 금액

    // 총 소비-유저 다대일 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_month", nullable = false, length = 7)
    private String createdMonth;

    @PrePersist
    public void onPrePersist() {
        this.createdMonth = this.getCreatedAt().toLocalDate().withDayOfMonth(1).toString().substring(0, 7); // "2025-07"
    }

    // 총 소비 금액 증가
    public void updateAddTotalConsumptionAmount(int amount) {
        this.totalConsumptionAmount += amount;
    }

    // 총 소비 금액 감소
    public void updateSubstractTotalConsumptionAmount(int amount) {
        this.totalConsumptionAmount -= amount;
    }

    // 총 소비 금액 수정 (기존 금액 → 새로운 금액)
    public void updateTotalConsumptionAmount(int oldAmount, int newAmount) {
        if (oldAmount < 0 || newAmount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다.");
        }

        int adjustedAmount = this.totalConsumptionAmount - oldAmount + newAmount;

        if (adjustedAmount < 0) {
            throw new IllegalStateException("총 소비 금액은 음수가 될 수 없습니다.");
        }

        this.totalConsumptionAmount = adjustedAmount;
    }
}
