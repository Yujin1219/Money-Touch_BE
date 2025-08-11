package com.server.money_touch.domain.consumptionRecord.service;

import com.server.money_touch.domain.consumptionRecord.converter.consumptionRecord.ConsumptionRecordConverter;
import com.server.money_touch.domain.consumptionRecord.converter.household.HouseholdConsumptionConverter;
import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.projection.DailyAmountProjection;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemDetailProjection;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemProjection;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory.ConsumptionCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
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
    private static final Integer PAGE_SIZE = 10;

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

        log.info("일일 소비 내역 조회 완료: userId: {}, consumptionRecordId: {}", userId, consumptionRecordId);
        return ConsumptionRecordConverter.toDailyConsumptionDetailDTO(consumptionRecord, consumptionCategory);
    }

    /**
     * 해당 월의 소비 내역 목록 조회 (커서 기반 무한스크롤)
     * - 요구사항: 페이지 사이즈와 관계없이 "특정 날짜의 데이터가 여러 페이지에 분리되지 않도록" 보장.
     * - 핵심 규칙: 월 범위는 반드시 [monthStart, nextMonthStart) 반열린 구간으로 통일 (상한 미만)
     *
     * 전략:
     *   1) (consumeDate DESC, id DESC) 기준으로 pageSize+1개를 가져와 "경계 날짜(boundaryDate)"를 결정
     *   2) 경계 날짜가 잘릴 위험이 있으므로, 경계 날짜의 "나머지 전부"를 추가 쿼리로 모아 합치기
     *      - 이때도 월 범위를 [monthStart, nextMonthStart)로 강제(클램핑)하여 월 넘김 혼입 방지
     *   3) 다음 페이지 존재 여부는 "경계 날짜보다 과거 데이터가 있는가"로 판정
     *   4) nextCursorId는 "이번에 내려준 마지막(가장 오래된) 아이템의 id"로 반환
     *      (레포에서 커서를 '날짜 기준'으로만 쓰도록 하면, 같은 날짜가 다음 페이지에 재등장하지 않음)
     */
    @Override
    public HouseholdConsumptionResponse.MonthlyHistoryResponseDTO getMonthlyConsumptionRecords(
            Long userId, int year, int month, Long cursorId) {

        // 0) 사용자 검증
        userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 1) 해당 월 범위 계산
        //    ⚠️ 월 범위는 반드시 [monthStart, nextMonthStart) 로 사용 (상한 미만)
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDateTime monthStart = firstDay.atStartOfDay();                 // ex) 2025-07-01 00:00
        LocalDateTime nextMonthStart = firstDay.plusMonths(1).atStartOfDay(); // ex) 2025-08-01 00:00

        // 2) 커서의 consumeDate 조회 (id → consumeDate)
        //    다음 페이지는 "이 날짜의 0시보다 과거(< dayStart)"만 보도록 만들 것이므로
        //    레포지토리에서 id 커서 비교(= 같은 날짜에서 id<...)는 사용하지 않게 해야 "날짜 쪼개짐"이 사라집니다.
        LocalDateTime cursorConsumeDate = null;
        if (cursorId != null) {
            cursorConsumeDate = consumptionRecordRepository.findConsumeDateById(cursorId);
            // 잘못된 id면 첫 페이지처럼 동작해도 무방 (정책에 따라 400 던져도 됨)
        }

        // 3) 1차 청크: pageSize+1
        //    - 정렬: consumeDate DESC, id DESC
        //    - 커서 조건: cursorConsumeDate != null 이면 (consumeDate < cursorDayStart)
        //      즉, "날짜만" 기준으로 다음 페이지를 자르도록 해야 같은 날짜 재등장이 없음.
        final int pageSize = PAGE_SIZE; // 기존 상수 재사용
        List<DailyConsumptionItemProjection> chunk = consumptionRecordRepository
                .findChunkByMonthUsingDateCursor(
                        userId,
                        monthStart,            // ✅ 하한: 포함
                        nextMonthStart,        // ✅ 상한: 미만
                        cursorConsumeDate,
                        pageSize + 1
                );

        if (chunk.isEmpty()) {
            return ConsumptionRecordConverter.toMonthlyHistoryResponseDTO(
                    List.of(), /*isFirst*/ true, /*hasNext*/ false, /*nextCursorId*/ null
            );
        }

        // 4) 경계 날짜(boundaryDate) 계산
        //    - "표시 대상"은 우선 chunk의 앞에서 pageSize개
        //    - 그중 마지막 아이템의 consumeDate(최소 consumeDate)가 경계 날짜가 됨
        int visibleCount = Math.min(pageSize, chunk.size());
        List<DailyConsumptionItemProjection> visible = new ArrayList<>(chunk.subList(0, visibleCount));

        LocalDate boundaryDate = visible.get(visibleCount - 1).getConsumeDate().toLocalDate();
        LocalDateTime boundaryStart = boundaryDate.atStartOfDay();                  // ex) 2025-07-31 00:00
        LocalDateTime boundaryEnd = boundaryStart.plusDays(1).minusNanos(1);       // ex) 2025-07-31 23:59:59.999999999

        // 4-1) 경계 날짜 구간도 월 경계를 넘지 않도록 '클램핑'
        //      - 하한은 monthStart 이상
        //      - 상한은 nextMonthStart 미만
        LocalDateTime boundaryStartClamped = boundaryStart.isBefore(monthStart) ? monthStart : boundaryStart;
        LocalDateTime boundaryEndClampedExclusive = boundaryEnd.plusNanos(1); // [start, end] → [start, endExclusive)
        LocalDateTime boundaryEndClampedExclusiveFinal =
                boundaryEndClampedExclusive.isAfter(nextMonthStart) ? nextMonthStart : boundaryEndClampedExclusive;
        // 최종적으로 between 용으로 다시 [start, end] 포함구간으로 변환
        LocalDateTime boundaryEndClamped = boundaryEndClampedExclusiveFinal.minusNanos(1);

        // 5) 경계 날짜의 나머지 아이템 전부 추가 조회
        //    - chunk는 pageSize 제한 때문에 "경계 날짜의 일부"만 담겼을 수 있음
        //    - 경계 날짜의 "나머지 전부"를 추가 조회하여 합쳐 한 날짜가 절대 쪼개지지 않도록 보장
        Long minIncludedIdOnBoundary = visible.stream()
                .filter(p -> p.getConsumeDate().toLocalDate().equals(boundaryDate))
                .map(DailyConsumptionItemProjection::getConsumptionRecordId)
                .min(Long::compareTo) // 정렬이 DESC이므로 "가장 오래된 id"가 min
                .orElse(Long.MAX_VALUE);

        List<DailyConsumptionItemProjection> extraSameDate =
                consumptionRecordRepository.findRestOfBoundaryDateClampedToMonth(
                        userId,
                        monthStart, nextMonthStart,           // ✅ 월 범위 [monthStart, nextMonthStart)
                        boundaryStartClamped, boundaryEndClamped, // ✅ 하루 범위도 월에 클램핑
                        minIncludedIdOnBoundary
                );

        // 6) 결과 합치기 (정렬 유지: consumeDate DESC, id DESC)
        //    - visible(앞부분) + extraSameDate(경계 날짜 나머지) 순으로 합칩니다.
        List<DailyConsumptionItemProjection> merged = new ArrayList<>(visible.size() + extraSameDate.size());
        merged.addAll(visible);
        merged.addAll(extraSameDate);

        // 7) hasNext 계산: 경계 날짜보다 과거 데이터가 있는지
        boolean hasNext = consumptionRecordRepository.existsOlderThanDate(
                userId, monthStart, nextMonthStart, boundaryStartClamped
        );

        // 8) nextCursorId: 이번에 내려준 리스트 중 "가장 오래된" 아이템의 id (마지막 요소)
        Long nextCursorId = hasNext && !merged.isEmpty()
                ? merged.get(merged.size() - 1).getConsumptionRecordId()
                : null;

        // 9) 날짜 단위 그룹핑 → 응답 DTO
        Map<LocalDate, List<HouseholdConsumptionResponse.DailyRecordDTO>> grouped = merged.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getConsumeDate().toLocalDate(),
                        TreeMap::new, // 날짜 오름차순
                        Collectors.mapping(ConsumptionRecordConverter::toDailyRecordDTO, Collectors.toList())
                ));

        // 날짜 최신순으로 정렬하여 DTO 구성
        List<HouseholdConsumptionResponse.DailyHistoryDTO> dailyHistory = grouped.entrySet().stream()
                .map(e -> ConsumptionRecordConverter.toDailyHistoryDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(HouseholdConsumptionResponse.DailyHistoryDTO::getDate).reversed())
                .toList();

        log.info("월 소비 내역(커서) - userId={}, {}-{}, cursorId={}, nextCursorId={}, boundaryDate={}, hasNext={}, mergedSize={}",
                userId, year, month, cursorId, nextCursorId, boundaryDate, hasNext, merged.size());

        return ConsumptionRecordConverter.toMonthlyHistoryResponseDTO(
                dailyHistory,
                cursorId == null,   // isFirst
                hasNext,
                nextCursorId
        );
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

        log.info("달력 월별 소비 금액 조회 완료: userId: {}, year: {}, month: {}", userId, year, month);

        // 5. DTO 생성 후 반환
        return HouseholdConsumptionResponse.CalendarDateAmountMapDTO.builder().data(result).build();
    }

    // 달력에서 특정 날짜의 소비 내역 상세 조회 (커서 기반 무한스크롤)
    @Override
    public HouseholdConsumptionResponse.CalendarDailyConsumeSliceResponse getCalendarDailyConsumptionRecordsDetail(Long userId, int year, int month, int day, Long cursorId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        LocalDate targetDate = LocalDate.of(year, month, day);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        // 커서 consumeDate 조회
        LocalDateTime cursorConsumeDate = null;
        if (cursorId != null) {
            cursorConsumeDate = consumptionRecordRepository.findConsumeDateById(cursorId);
        }

        // 데이터 조회 (Slice)
        Slice<DailyConsumptionItemDetailProjection> slice = consumptionRecordRepository
                .findDailyConsumptionItemsWithCursor(userId, startOfDay, endOfDay, cursorId, cursorConsumeDate, PAGE_SIZE);

        List<HouseholdConsumptionResponse.ConsumeItemDTO> items = slice.getContent().stream()
                .map(HouseholdConsumptionConverter::toConsumeItem)
                .toList();

        Long nextCursorId = (slice.hasNext() && !items.isEmpty())
                ? items.get(items.size() - 1).getConsumptionRecordId()
                : null;
        boolean isFirst = (cursorId == null);

        log.info("달력 특정 날짜의 소비 내역 상세 조회(커서 기반 무한스크롤) 완료 - userId: {}, year: {}, month: {}, cursorId: {}, nextCursorId: {}", userId, year, month, cursorId, nextCursorId);

        return ConsumptionRecordConverter.toCalendarDailyConsumeSliceResponse(
                targetDate,
                slice,
                nextCursorId,
                isFirst
        );
    }
}
