package com.server.money_touch.domain.routine.converter;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.routine.dto.RoutineRequest;
import com.server.money_touch.domain.routine.dto.RoutineResponse;
import com.server.money_touch.domain.routine.entity.Routine;
import com.server.money_touch.domain.user.entity.User;

public class RoutineConverter {
    // 루틴 엔티티 생성
    public static Routine toRoutine(User user, Budget budget, RoutineRequest.RoutineCreateDTO routineCreateDTO) {
        return Routine.builder()
                .user(user)
                .budget(budget)
                .routineName(routineCreateDTO.getRoutineName())
                .routineContent("") // 루틴 설명은 삭제 됐기 때문에 추후 수정
                .routineImageUrl(routineCreateDTO.getRoutineImgUrl())
                .viewCount(0)
                .build();
    }

    // 루틴 생성 응답 DTO
    public static RoutineResponse.RoutineCreateResultDTO toRoutineCreateResultDTO(Long routineId) {
        return RoutineResponse.RoutineCreateResultDTO.builder()
                .routineId(routineId)
                .build();
    }
}
