package com.server.money_touch.domain.routine.converter;

import com.server.money_touch.domain.routine.dto.RoutineRequest;
import com.server.money_touch.domain.routine.entity.Routine;
import com.server.money_touch.domain.routine.entity.RoutineAmount;

public class RoutineAmountConverter {

    // RoutineAmount 변환
    public static RoutineAmount toRoutineAmount(RoutineRequest.CategoryBudgetDTO dto, Routine routine) {
        return RoutineAmount.builder()
                .categoryName(dto.getCategoryName())
                .amount(dto.getAmount())
                .routine(routine)
                .build();
    }

}
