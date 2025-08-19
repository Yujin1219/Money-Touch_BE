package com.server.money_touch.domain.consumptionRecord.service;

import com.server.money_touch.domain.consumptionRecord.converter.consumptionRecord.ConsumptionRecordConverter;
import com.server.money_touch.domain.consumptionRecord.converter.totalConsumption.TotalConsumptionConverter;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordResponse;
import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionRequest;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.entity.TotalConsumption;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory.ConsumptionCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.consumptionRecord.repository.totalConsumption.TotalConsumptionRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ConsumptionRecordCommandServiceImpl implements ConsumptionRecordCommandService {

    private final ConsumptionRecordRepository consumptionRecordRepository;
    private final ConsumptionCategoryRepository consumptionCategoryRepository;
    private final UserRepository userRepository;

    // 일일 소비 기록 등록
    @Transactional
    @Override
    public ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO saveDailyConsumptionRecord(Long userId, HouseholdConsumptionRequest.DailyConsumptionCreateDTO request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 카테고리 이름으로 유저의 소비 카테고리 테이블 조회
        ConsumptionCategory consumptionCategory = consumptionCategoryRepository.findByUserAndBudgetCategoryName(user, request.getCategoryName())
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.CONSUMPTION_CATEGORY_NAME_NOT_FOUND));

        // 3. 소비 기록 엔티티 생성
        ConsumptionRecord dailyConsumptionRecord = ConsumptionRecordConverter.toDailyConsumptionRecord(user, consumptionCategory, request, false);
        consumptionRecordRepository.save(dailyConsumptionRecord);

        Long consumptionRecordId = dailyConsumptionRecord.getId();
        log.info("일일 소비 기록 등록 완료: userId: {}, consumptionRecordId: {}", userId, consumptionRecordId);
        return ConsumptionRecordConverter.toConsumptionRecordCreateResultDTO(consumptionRecordId);
    }

    // 일일 소비 기록 수정
    @Transactional
    @Override
    public void updateDailyConsumptionRecord(Long userId, Long consumptionRecordId, HouseholdConsumptionRequest.DailyConsumptionCreateDTO request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 소비 기록 아이디로 유저의 소비 기록 테이블 조회
        ConsumptionRecord consumptionRecord = consumptionRecordRepository.findById(consumptionRecordId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND));

        // 3. 카테고리 이름으로 유저의 소비 카테고리 테이블 조회
        ConsumptionCategory consumptionCategory = consumptionCategoryRepository.findByUserAndBudgetCategoryName(user, request.getCategoryName())
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.CONSUMPTION_CATEGORY_NAME_NOT_FOUND));

        // 4. 일일 소비 기록 수정
        consumptionRecord.updateDailyConsumptionRecord(consumptionCategory, request.getAmount(), request.getContent(),request.getMemo(), request.getConsumeDate());

        log.info("일일 소비 기록 수정 완료 - userId: {}, consumptionRecordId: {}", userId, consumptionRecordId);
    }

    // 일일 소비 기록 삭제
    @Transactional
    @Override
    public void deleteDailyConsumptionRecord(Long userId, Long consumptionRecordId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 소비 기록 아이디로 유저의 소비 기록 테이블 조회
        ConsumptionRecord consumptionRecord = consumptionRecordRepository.findById(consumptionRecordId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND));

        // 3. 소비 기록 삭제
        consumptionRecordRepository.delete(consumptionRecord);

        log.info("일일 소비 기록 삭제 완료 - userId: {}, consumptionRecordId: {}", userId, consumptionRecordId);
    }

}
