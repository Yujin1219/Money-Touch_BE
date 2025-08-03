package com.server.money_touch.domain.routine.entity;

import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class RoutineAmount extends BaseEntity {
    // 카테고리 이름: 술/유흥, 데이틑 등
    @Column(length = 8, nullable = false)
    private String categoryName;

//    // 카테고리 타입: 소비 루틴 카테고리?
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private CategoryType categoryType;

    @Column(columnDefinition = "INT DEFAULT 0",  nullable = false)
    private Integer amount; // 금액

    // 소비 루틴 금액-소비루틴 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private Routine routine;
}
