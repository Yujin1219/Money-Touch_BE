package com.server.money_touch.domain.consumptionMbti.service;

import com.server.money_touch.domain.consumptionMbti.converter.ConsumptionMbtiConveter;
import com.server.money_touch.domain.consumptionMbti.dto.ConsumptionMbtiResponse;
import com.server.money_touch.domain.consumptionMbti.entity.ConsumptionMbti;
import com.server.money_touch.domain.consumptionMbti.repository.ConsumptionMbtiRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsumptionMbtiServiceImpl implements ConsumptionMbtiService{

    private final ConsumptionMbtiRepository consumptionMbtiRepository;

    // 소비 MBTI 존재 여부 검증
    @Override
    public Boolean existsConsumptionMbti(Long ConsumptionMbtiId) {
        return consumptionMbtiRepository.existsById(ConsumptionMbtiId);
    }


    @Override
    public ConsumptionMbtiResponse.ConsumptionMbtiResultDTO getConsumptionMbti(String code) {
        ConsumptionMbti consumptionMbti = consumptionMbtiRepository.findByCode(code)
                .orElseThrow(()-> new ErrorHandler(ErrorStatus.MBTI_NOT_FOUND));
        return ConsumptionMbtiConveter.toResultDTO(consumptionMbti);
    }

}
