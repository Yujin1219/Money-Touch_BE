package com.server.money_touch.domain.fixedConsumption.entity;

import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class FixedConsumption extends BaseEntity {
    @Column(columnDefinition = "INT DEFAULT 0", nullable = false)
    private Integer fixedConsumptionAmount;

    @Column(nullable = false, length = 20)
    private String fixedConsumptionContent;

    @Column(nullable = false, length = 1000)
    private String fixedConsumptionMemo;

    // 고정비-카테고리별 예산 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_category_id")
    private BudgetCategory budgetCategory;
}
