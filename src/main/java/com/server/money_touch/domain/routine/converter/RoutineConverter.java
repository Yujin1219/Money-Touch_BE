package com.server.money_touch.domain.routine.converter;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.routine.dto.RoutineRequest;
import com.server.money_touch.domain.routine.dto.RoutineResponse;
import com.server.money_touch.domain.routine.entity.Routine;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.domain.Slice;

import java.util.List;

public class RoutineConverter {
    // 루틴 엔티티 생성
    public static Routine toRoutine(User user, Budget budget, RoutineRequest.RoutineCreateDTO routineCreateDTO) {
        return Routine.builder()
                .user(user)
                .budget(budget)
                .routineName(routineCreateDTO.getRoutineName())
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

    // 루틴 이미지 생성 응답 DTO
    public static RoutineResponse.RoutineImageUrlDTO toRoutineImageUrlDTO(String imageUrl) {
        return RoutineResponse.RoutineImageUrlDTO.builder().routineImageUrl(imageUrl).build();
    }

    // 루틴 상세 정보 응답 DTO
    public static RoutineResponse.RoutineDetailDTO toRoutineDetailDTO(Integer totalBudget, String routineName, List<RoutineResponse.CategoryBudgetDetailDTO> categoryBudgetList) {
        return RoutineResponse.RoutineDetailDTO
                .builder()
                .totalBudget(totalBudget)
                .routineName(routineName)
                .categoryBudgetList(categoryBudgetList)
                .build();
    }

    // 내 소비 루틴 목록 조회 응답 DTO
    public static RoutineResponse.MyRoutineListDTO toMyRoutineListDTO(List<RoutineResponse.RoutineThumbnailDTO> routineList, Slice<RoutineResponse.RoutineThumbnailDTO> slice) {
        return RoutineResponse.MyRoutineListDTO.builder()
                .routineList(routineList)
                .routineListSize(routineList.size())
                .isFirst(slice.isFirst())
                .isLast(slice.isLast())
                .hasNext(slice.hasNext())
                .nextCursorId(slice.hasNext() ? routineList.get(routineList.size() - 1).getRoutineId() : null)
                .build();
    }
}
