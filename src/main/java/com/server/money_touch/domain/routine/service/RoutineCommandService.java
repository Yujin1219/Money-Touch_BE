package com.server.money_touch.domain.routine.service;

import com.server.money_touch.domain.routine.dto.RoutineRequest;
import com.server.money_touch.domain.routine.dto.RoutineResponse;
import com.server.money_touch.global.validation.annotation.ExistBudget;
import com.server.money_touch.global.validation.annotation.ExistRoutine;
import com.server.money_touch.global.validation.annotation.ExistUser;

public interface RoutineCommandService {
    // 소비 루틴 등록
    RoutineResponse.RoutineCreateResultDTO saveRoutineWithRoutineHashtags(@ExistUser Long userId, @ExistBudget Long budgetId, RoutineRequest.RoutineCreateDTO request);

    // 타인의 소비 루틴을 내 예산에 반영
    void applyRoutineToBudget(@ExistUser Long userId, @ExistBudget Long budgetId, @ExistRoutine Long routineId, RoutineRequest.ApplyRoutineBudgetDTO request);
}
