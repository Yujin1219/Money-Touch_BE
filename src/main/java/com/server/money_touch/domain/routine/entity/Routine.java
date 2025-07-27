package com.server.money_touch.domain.routine.entity;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class Routine extends BaseEntity {
    @Column(length = 20, nullable = false)
    private String routineName;

    @Column(nullable = false)
    private String routineImageUrl;

    @Column(columnDefinition = "INT DEFAULT 0", nullable = false)
    private Integer viewCount; // 조회수

    // 소비루틴-예산 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    // 소비루틴-유저 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
