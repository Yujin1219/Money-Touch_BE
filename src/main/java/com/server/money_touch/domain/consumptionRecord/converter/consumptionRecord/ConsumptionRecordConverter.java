package com.server.money_touch.domain.consumptionRecord.converter.consumptionRecord;

import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordResponse;
import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionRequest;
import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.user.entity.User;

public class ConsumptionRecordConverter {

    // 일일 소비 기록 시 소비 카테고리 엔티티 생성
    public static ConsumptionRecord toDailyConsumptionRecord(User user, ConsumptionCategory consumptionCategory, HouseholdConsumptionRequest.DailyConsumptionCreateDTO requestDTO) {
        return ConsumptionRecord.builder()
                .user(user)
                .consumptionCategory(consumptionCategory)
                .amount(requestDTO.getAmount())
                .content(requestDTO.getContent())
                .memo(requestDTO.getMemo())
                .consumeDate(requestDTO.getConsumeDate())
                .isPublic(true) // 가계부에만 등록
                .commentCount(0)
                .wiseCount(0)
                .wasteCount(0)
                .viewCount(0)
                .build();
    }

    // 소비 기록 응답
    public static ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO toConsumptionRecordCreateResultDTO(Long consumptionRecordId){
        return ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO.builder()
                .consumptionRecordId(consumptionRecordId)
                .build();
    }

    // 일일 소비 기럭 내역 조회 응답
    public static HouseholdConsumptionResponse.DailyConsumptionDetailDTO toDailyConsumptionDetailDTO(ConsumptionRecord consumptionRecord, ConsumptionCategory consumptionCategory){
        return HouseholdConsumptionResponse.DailyConsumptionDetailDTO.builder()
                .categoryName(consumptionCategory.getBudgetCategoryName())
                .amount(consumptionRecord.getAmount())
                .content(consumptionRecord.getContent())
                .memo(consumptionRecord.getMemo())
                .consumeDate(consumptionRecord.getConsumeDate())
                .build();
    }
}
