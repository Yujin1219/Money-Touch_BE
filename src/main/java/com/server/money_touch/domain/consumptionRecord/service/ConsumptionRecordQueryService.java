package com.server.money_touch.domain.consumptionRecord.service;

import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionResponse;
import com.server.money_touch.global.validation.annotation.ExistConsumptionRecord;
import com.server.money_touch.global.validation.annotation.ExistUser;

public interface ConsumptionRecordQueryService {
    // 소비 기록 존재 여부 검증
    Boolean existsConsumptionRecordById(Long consumptionRecordId);

    // 일일 소비 내역 조회
    HouseholdConsumptionResponse.DailyConsumptionDetailDTO getDailyConsumptionRecordDetail(@ExistUser Long userId, @ExistConsumptionRecord Long consumptionRecordId);
}
