package com.server.money_touch.routine.entity;

import com.server.money_touch.budget.entity.Budget;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class Routine extends BaseEntity {
    @Size(max = 20)
    @Column(length = 60, nullable = false)
    private String routineName;

    @Size(max = 1000)
    @Column(length = 3000, nullable = false)
    private String routineContent;

    @Column(nullable = false)
    private String routineImageUrl;

    @Column(columnDefinition = "INT DEFAULT 0", nullable = false)
    private Integer viewCount; // 조회수

    // 소비루틴-예산 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    // 소비루틴-유저 다대일
}
