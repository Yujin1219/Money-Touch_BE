package com.server.money_touch.domain.fixedConsumption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FixedConsumptionResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "고정비 등록 응답 정보")
    public static class FixedConsumptionCreateResultDTO {
        @Schema(description = "고정비 아이디", example = "1")
        private Long fixedConsumptionId;
    }
}
