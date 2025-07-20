package com.server.money_touch.domain.consumptionRecord.service;

import com.server.money_touch.domain.consumptionRecord.converter.consumptionRecord.ConsumptionRecordConverter;
import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory.ConsumptionCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.respotiroy.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ConsumptionRecordQueryServiceImpl implements ConsumptionRecordQueryService {

    private final ConsumptionRecordRepository consumptionRecordRepository;
    private final UserRepository userRepository;
    private final ConsumptionCategoryRepository consumptionCategoryRepository;

    // 소비 기록 존재 여부 검증
    @Override
    public Boolean existsConsumptionRecordById(Long consumptionRecordId) {
        return consumptionRecordRepository.findById(consumptionRecordId).isPresent();
    }

    // 일일 소비 내역 조회
    @Override
    public HouseholdConsumptionResponse.DailyConsumptionDetailDTO getDailyConsumptionRecordDetail(Long userId, Long consumptionRecordId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 소비 기록 아이디로 유저의 소비 기록 테이블 조회
        ConsumptionRecord consumptionRecord = consumptionRecordRepository.findById(consumptionRecordId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND));

        // 3. 소비 기록과 연관된 소비 카테고리 테이블 조회
        ConsumptionCategory consumptionCategory = consumptionCategoryRepository.findCategoryByConsumptionRecordId(consumptionRecordId)
                .orElseThrow(() -> new IllegalArgumentException(" 소비 기록과 연관된 소비 카테고리 테이블이 존재하지 않습니다. 관리자에게 문의해 주세요."));

        log.info("일일 소비 내역 조회 완료, consumptionRecordId: {}", consumptionRecordId);
        return ConsumptionRecordConverter.toDailyConsumptionDetailDTO(consumptionRecord, consumptionCategory);
    }
}
