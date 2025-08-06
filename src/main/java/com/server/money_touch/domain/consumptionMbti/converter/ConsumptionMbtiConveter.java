package com.server.money_touch.domain.consumptionMbti.converter;

import com.server.money_touch.domain.consumptionMbti.dto.ConsumptionMbtiResponse;
import com.server.money_touch.domain.consumptionMbti.entity.ConsumptionMbti;

public class ConsumptionMbtiConveter {

    public static ConsumptionMbtiResponse.ConsumptionMbtiResultDTO toResultDTO(ConsumptionMbti entity) {
        return new ConsumptionMbtiResponse.ConsumptionMbtiResultDTO(
                entity.getId(),
                entity.getResult(),
                entity.getSubtitle(),
                entity.getMbtiImgUrl(),
                entity.getDescription()
        );

    }
}
