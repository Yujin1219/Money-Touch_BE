package com.server.money_touch.domain.consumptionMbti.service;


import com.server.money_touch.domain.consumptionMbti.dto.ConsumptionMbtiResponse;

public interface ConsumptionMbtiService {
    // 소비 MBTI 존재 여부 검증
    Boolean existsConsumptionMbti(Long consumptionMbtiId);

    // 소비 MBTI 조회
    ConsumptionMbtiResponse.ConsumptionMbtiResultDTO getConsumptionMbti(String code);

}
