package com.server.money_touch.domain.budget.entity;

import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
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

    public void updateTotalBudget(Integer budgetTotal) {
        this.budgetTotal = budgetTotal;
    }
}
