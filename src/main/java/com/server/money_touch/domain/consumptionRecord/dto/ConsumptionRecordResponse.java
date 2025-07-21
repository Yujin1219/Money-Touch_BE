package com.server.money_touch.domain.consumptionRecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

public class ConsumptionRecordResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소비 기록 등록 응답 정보")
    public static class ConsumptionRecordCreateResultDTO {

        @Schema(description = "소비 기록 아이디", example = "1")
        private Long consumptionRecordId;

    }
}
