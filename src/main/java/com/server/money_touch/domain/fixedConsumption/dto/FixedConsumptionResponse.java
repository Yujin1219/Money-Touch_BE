package com.server.money_touch.domain.fixedConsumption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "고정비 조회 목록 응답 정보")
    public static class FixedConsumptionCursorResultDTO {

        @Schema(description = "고정비 목록")
        private List<FixedConsumptionDetailDTO> fixedConsumptions;

        @Schema(description = "고정비 목록 총 개수", example = "25")
        private Integer fixedConsumptionSize;

        @Schema(description = "첫 페이지 여부", example = "true")
        private Boolean isFirst;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private Boolean isLast;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private Boolean hasNext;

        @Schema(description = "다음 요청에 사용할 커서 (마지막 고정비 ID)", example = "3")
        private Long nextCursorId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "고정비 조회 목록 세부 응답 정보")
    public static class FixedConsumptionDetailDTO {
        @Schema(description = "고정비 아이디", example = "1")
        private Long fixedConsumptionId;

        @Schema(description = "소비 카테고리 이름", example = "기타")
        private String categoryName;

        @Schema(description = "고정비 금액", example = "23000")
        private Integer amount;

        @Schema(description = "메모", example = "가족 공유 요금제")
        private String memo;
    }
}
