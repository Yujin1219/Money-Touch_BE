package com.server.money_touch.routine.entity;

import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class RoutineHashtag extends BaseEntity {
    @Size(max = 20)
    @Column(length = 60, nullable = false)
    private String routineHashtagName;

    // 소비 루틴 해시태그-소비루틴 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private Routine routine;
}
