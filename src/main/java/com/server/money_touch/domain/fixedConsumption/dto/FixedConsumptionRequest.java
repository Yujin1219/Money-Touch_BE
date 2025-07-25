package com.server.money_touch.domain.fixedConsumption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class FixedConsumptionRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "고정비 등록 요청 정보")
    public static class FixedConsumptionCreateDTO{

        @Schema(description = "고정비 금액", example = "20000")
        @NotNull(message = "고정비 금액은 필수입니다.")
        private Integer amount;

        @Schema(description = "소비 카테고리 이름", example = "기타")
        @NotNull(message = "소비 카테고리 이름은 필수입니다.")
        private String categoryName;

        @Schema(description = "항목명", example = "구독 요금")
        @NotNull(message = "항목명 이름은 필수입니다.")
        @Size(max = 20, message = "항목명은 20자 이하로 입력해주세요.")
        private String content;

        @Schema(description = "메모", example = "가족 공유 요금제")
        @NotNull(message = "메모는 필수입니다.")
        @Size(max = 1000, message = "항목명은 1000자 이하로 입력해주세요.")
        private String memo;
    }
}
