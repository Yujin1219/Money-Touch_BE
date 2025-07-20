package com.server.money_touch.domain.consumptionRecord.service;

import com.server.money_touch.domain.consumptionRecord.converter.consumptionRecord.ConsumptionRecordConverter;
import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.projection.DailyAmountProjection;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemDetailProjection;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemProjection;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ConsumptionRecordQueryServiceImpl implements ConsumptionRecordQueryService {

    private final ConsumptionRecordRepository consumptionRecordRepository;
    private final UserRepository userRepository;
    private final ConsumptionCategoryRepository consumptionCategoryRepository;
    private static final Integer PAGE_SIZE = 15;

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

    // 달력에서 특정 날짜의 소비 내역 상세 조회
    @Override
    public HouseholdConsumptionResponse.CalendarDailyConsumeDetailDTO getCalendarDailyConsumptionRecordsDetail(Long userId, int year, int month, int day) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 조회 대상 날짜 생성
        LocalDate targetDate = LocalDate.of(year, month, day);

        // 3. 해당 날짜의 소비 내역 조회 (Projection 형태)
        List<DailyConsumptionItemDetailProjection> projections = consumptionRecordRepository.findDailyConsumptionItems(userId, targetDate);

        // 4. Projection → DTO 변환
        List<HouseholdConsumptionResponse.ConsumeItemDTO> items = projections.stream()
                .map(p -> HouseholdConsumptionResponse.ConsumeItemDTO.builder()
                        .consumptionRecordId(p.getConsumptionRecordId())
                        .categoryName(p.getCategoryName())
                        .content(p.getContent())
                        .amount(p.getAmount())
                        .build())
                .toList();

        log.info("달력 - 특정 날짜의 소비 내역 조회 완료, targetDate: {}", targetDate);

        // 5. 변환된 결과를 응답 DTO로 매핑하여 반환
        return ConsumptionRecordConverter.toCalendarDailyConsumeDetailDTO(targetDate, items);
    }

    // 가계부 달력 월별 소비 금액 조회
    @Override
    public HouseholdConsumptionResponse.CalendarDateAmountMapDTO getMonthlyConsumptionCalendar(Long userId, int year, int month) {
        // 1. 사용자 존재 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 해당 월의 시작일과 마지막 날짜 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 3. 소비 기록에서 일별 총 소비 금액 조회 (날짜 기준 group by)
        List<DailyAmountProjection> projections = consumptionRecordRepository
                .findDailyTotalAmounts(userId, startDate, endDate);

        // 4. 소비일 기준 오름차순 정렬된 Map 생성
        Map<String, Integer> result = projections.stream()
                .sorted(Comparator.comparing(DailyAmountProjection::getDate)) // LocalDate 기준 정렬
                .collect(Collectors.toMap(
                        p -> p.getDate().toString(),            // key: 날짜 문자열
                        DailyAmountProjection::getTotalAmount, // value: 총 소비 금액
                        (v1, v2) -> v1,                         // key 충돌 시 첫 번째 값 유지
                        LinkedHashMap::new                      // 순서 유지
                ));

        log.info("달력 - 월별 소비 금액 조회 완료, year: {}, month: {}", year, month);

        // 5. DTO 생성 후 반환
        return HouseholdConsumptionResponse.CalendarDateAmountMapDTO.builder().data(result).build();
    }

    // 가계부 달력 해당 월의 소비 내역 목록 조회 (커서 기반 무한스크롤)
    @Override
    public HouseholdConsumptionResponse.MonthlyHistoryResponseDTO getMonthlyConsumptionRecords(Long userId, int year, int month, Long cursorId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 해당 월의 시작일과 종료일 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 커서 ID가 존재할 경우, 해당 ID의 consumeDate 조회
        LocalDateTime cursorConsumeDate = null;
        if (cursorId != null) {
            cursorConsumeDate = consumptionRecordRepository.findConsumeDateById(cursorId);
        }

        // 소비 기록 페이징 조회 (consumeDate + id 최신순 기준)
        List<DailyConsumptionItemProjection> fetched = consumptionRecordRepository
                .findMonthlyConsumptionItems(userId, startDate, endDate, cursorId, cursorConsumeDate, PAGE_SIZE);

        // 다음 페이지 존재 여부 판단
        boolean hasNext = fetched.size() > PAGE_SIZE;
        List<DailyConsumptionItemProjection> content = hasNext ? fetched.subList(0, PAGE_SIZE) : fetched;

        // 날짜(LocalDate) 기준으로 그룹핑 (TreeMap: 날짜 오름차순 정렬)
        Map<LocalDate, List<HouseholdConsumptionResponse.DailyRecordDTO>> grouped = content.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getConsumeDate().toLocalDate(),
                        TreeMap::new,
                        Collectors.mapping(ConsumptionRecordConverter::toDailyRecordDTO, Collectors.toList())
                ));

        // 그룹핑된 데이터를 DTO로 변환 + 날짜 최신순 정렬
        List<HouseholdConsumptionResponse.DailyHistoryDTO> dailyHistory = grouped.entrySet().stream()
                .map(entry -> ConsumptionRecordConverter.toDailyHistoryDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(HouseholdConsumptionResponse.DailyHistoryDTO::getDate).reversed())
                .toList();

        // 다음 커서 ID 설정
        Long nextCursorId = content.isEmpty() ? null : content.get(content.size() - 1).getConsumptionRecordId();

        log.info("달력 - 해당 월의 소비 내역 목록 조회(커서 기반 무한스크롤) 완료, year: {}, month: {}, cursorId: {}, nextCursorId: {}", year, month, cursorId, nextCursorId);

        // 최종 응답 DTO 반환
        return ConsumptionRecordConverter.toMonthlyHistoryResponseDTO(
                dailyHistory,
                cursorId == null,   // isFirst
                hasNext,
                nextCursorId
        );
    }
}
