package com.server.money_touch.domain.consumptionMbti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

public class ConsumptionMbtiResponse {

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(description = "소비 Mbti 조회 응답 정보")
    public static class ConsumptionMbtiResultDTO{

        @Schema(description = "소비 Mbti 아이디", example = "1")
        private Long mbtiId;

        @Schema(description = "소비 mbti 이름", example = "PTG")
        private String result;

        @Schema(description = "소비 mbti 부제" , example = "계획 철벽러")
        private String subtitle;

        @Schema(description = "소비 mbti 설명" ,example = "철저한 계획 아래~")
        private String description;

        @Schema(description = "소비 mbti 이미지", example = "https://example.com/mbti.jpg")
        private String mbtiImgUrl;
    }
}
