package com.server.money_touch.domain.routine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class RoutineResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소비 루틴 등록 응답 정보")
    public static class RoutineCreateResultDTO {
        @Schema(description = "소비 루틴 아이디", example = "1")
        private Long routineId;
    }
}
