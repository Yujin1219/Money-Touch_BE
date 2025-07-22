package com.server.money_touch.domain.routine.converter;

import com.server.money_touch.domain.routine.entity.Routine;
import com.server.money_touch.domain.routine.entity.RoutineHashtag;

public class RoutineHashtagConverter {

    // 루틴 해시태그 엔티티 생성
    public static RoutineHashtag toRoutineHashtag(Routine routine, String routineHashtagName) {
        return RoutineHashtag.builder()
                .routine(routine)
                .routineHashtagName(routineHashtagName)
                .build();
    }
}
