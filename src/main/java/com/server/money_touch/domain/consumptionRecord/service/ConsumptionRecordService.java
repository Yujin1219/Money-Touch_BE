package com.server.money_touch.domain.consumptionRecord.service;

import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordRequest;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordResponse;
import com.server.money_touch.global.validation.annotation.ExistUser;

public interface ConsumptionRecordService {
    ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO createConsumptionRecord(Long userId, ConsumptionRecordRequest.ConsumptionRecordCreateDTO request);
}
