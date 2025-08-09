package com.server.money_touch.domain.consumptionRecord.converter.household;

import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionResponse;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemDetailProjection;

public class HouseholdConsumptionConverter {

    // 달력에서 특정 날짜의 소비 내역 상세 조회 응답 DTO 변환
    public static HouseholdConsumptionResponse.ConsumeItemDTO toConsumeItem(DailyConsumptionItemDetailProjection dailyConsumptionItem) {
        return HouseholdConsumptionResponse.ConsumeItemDTO.builder()
                .consumptionRecordId(dailyConsumptionItem.getConsumptionRecordId())
                .categoryName(dailyConsumptionItem.getCategoryName())
                .content(dailyConsumptionItem.getContent())
                .amount(dailyConsumptionItem.getAmount())
                .build();

    }
}
