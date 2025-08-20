package com.server.money_touch.domain.routine.entity;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_user_budget_month",
                        columnNames = {"user_id", "budget_id", "created_month"}
                )
        }
)
public class Routine extends BaseEntity {
    @Column(length = 20, nullable = false)
    private String routineName;

    @Column(nullable = false)
    private String routineImageUrl;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer routineTotalAmount; // 소비 루틴 총 금액

    // 생성일, "YYYY-MM" 형식으로 저장 (예: 2025-08)
    @Column(name = "created_month", nullable = false, length = 7)
    private String createdMonth;

    // 소비루틴-예산 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    // 소비루틴-유저 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 루틴-루틴해시태그 일대다
    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineHashtag> hashtags = new ArrayList<>();

    // 루틴-루틴Amount 일대다
    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineAmount> routineAmounts = new ArrayList<>();
}
