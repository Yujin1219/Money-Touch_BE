package com.server.money_touch.domain.consumptionRecord.entity;

import com.server.money_touch.domain.budget.entity.BudgetCategory;
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
public class ConsumptionRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_category_id", nullable = false)
    private BudgetCategory budgetCategory;

    @Column(nullable = false)
    private int amount;

    @Column(length = 20, nullable = false)
    private String content;

    private boolean isPublic = true;

    private String imageUrl;

    @Column(length = 1000)
    private String memo;


}
