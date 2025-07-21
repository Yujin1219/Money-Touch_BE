package com.server.money_touch.domain.consumptionRecord.service;

import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionCategoryResponse;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordRequest;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordResponse;

import java.util.List;

public interface ConsumptionRecordService {

    ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO createConsumptionRecord(Long userId, ConsumptionRecordRequest.ConsumptionRecordCreateDTO request);

    List<ConsumptionCategoryResponse.CategoryInfoDTO> getSortedCategoriesForUser(Long userId);
}
