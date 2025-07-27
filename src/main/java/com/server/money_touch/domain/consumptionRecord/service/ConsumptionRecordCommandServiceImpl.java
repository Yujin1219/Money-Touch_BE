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
    private final TotalConsumptionRepository totalConsumptionRepository;

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
        ConsumptionRecord dailyConsumptionRecord = ConsumptionRecordConverter.toDailyConsumptionRecord(user, consumptionCategory, request);
        consumptionRecordRepository.save(dailyConsumptionRecord);

        // 4-1. 현재 연도와 월 기준으로 월 시작일과 종료일 계산
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        // 4-2. 해당 월의 총 소비 금액 조회, 데이터가 없다면 생성
        TotalConsumption totalConsumption = totalConsumptionRepository
                .findByUserAndCreatedAtBetween(user, startOfMonth, endOfMonth)
                .orElseGet(() -> totalConsumptionRepository.save(TotalConsumptionConverter.toTotalConsumption(user)));

        // 4-3. 소비 금액 추가
        totalConsumption.updateAddTotalConsumptionAmount(request.getAmount());

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

        // 4-1. 현재 연도와 월 기준으로 월 시작일과 종료일 계산
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        // 4-2. 해당 월의 총 소비 금액 조회, 데이터가 없다면 에러
        TotalConsumption totalConsumption = totalConsumptionRepository
                .findByUserAndCreatedAtBetween(user, startOfMonth, endOfMonth)
                .orElseThrow(() -> new IllegalArgumentException("총 소비 정보가 존재하지 않습니다. 관리자에게 문의해 주세요."));

        // 4-3. 총 소비 금액 수정
        totalConsumption.updateTotalConsumptionAmount(consumptionRecord.getAmount(), request.getAmount());

        // 5. 일일 소비 기록 수정
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

        // 3-1. 현재 연도와 월 기준으로 월 시작일과 종료일 계산
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        // 3-2. 해당 월의 총 소비 금액 조회, 데이터가 없다면 에러
        TotalConsumption totalConsumption = totalConsumptionRepository
                .findByUserAndCreatedAtBetween(user, startOfMonth, endOfMonth)
                .orElseThrow(() -> new IllegalArgumentException("총 소비 정보가 존재하지 않습니다. 관리자에게 문의해 주세요."));

        // 3-3. 총 소비 금액 차감
        totalConsumption.updateSubstractTotalConsumptionAmount(consumptionRecord.getAmount());

        // 6. 소비 기록 삭제
        consumptionRecordRepository.delete(consumptionRecord);

        log.info("일일 소비 기록 삭제 완료 - userId: {}, consumptionRecordId: {}", userId, consumptionRecordId);
    }

}
