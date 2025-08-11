package com.server.money_touch.domain.consumptionMbti.dto;

import lombok.*;

public class ConsumptionMbtiRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumptionMbtiRequestDTO {
        private String result;       // 소비 MBTI 이름
        private String subtitle;     // 부제
        private String description;  // 설명
    }
}
