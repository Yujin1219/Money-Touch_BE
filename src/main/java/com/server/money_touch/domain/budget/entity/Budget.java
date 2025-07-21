package com.server.money_touch.domain.budget.entity;

import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
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
public class Budget extends BaseEntity {
    @Column(columnDefinition = "INT DEFAULT 0", nullable = false)
    private Integer budgetTotal;

    // 가져온 소비 루틴 여부, default는 false
    @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    @Builder.Default
    private Boolean isFromRoutine = false;

    // 예산-유저 다대일 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_month", nullable = false, length = 7)
    private String createdMonth;

    @PrePersist
    public void onPrePersist() {
        this.createdMonth = this.getCreatedAt().toLocalDate().withDayOfMonth(1).toString().substring(0, 7); // "2025-07"
    }

    public void updateTotalBudget(Integer budgetTotal) {
        this.budgetTotal = budgetTotal;
    }
}
