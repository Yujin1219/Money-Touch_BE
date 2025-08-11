package com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord;

import com.server.money_touch.domain.consumptionRecord.projection.DailyAmountProjection;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemDetailProjection;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemProjection;
import org.springframework.data.domain.Slice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ConsumptionRecordRepositoryCustom {
    // 사용자의 특정 날짜에 해당하는 소비 기록 목록을 조회
    Slice<DailyConsumptionItemDetailProjection> findDailyConsumptionItemsWithCursor(
            Long userId, LocalDateTime start, LocalDateTime end,
            Long cursorId, LocalDateTime cursorConsumeDate, int pageSize);

    // 해당 월의 소비내역을 커서 기반으로 페이지 단위 조회 (consumeDate 기준).
    List<DailyConsumptionItemProjection> findChunkByMonthUsingDateCursor(
            Long userId,
            LocalDateTime monthStart, LocalDateTime monthEnd,
            LocalDateTime cursorConsumeDate,
            int limit);

    // 경계 날짜(boundary date)의 나머지 데이터를 추가 조회.
    List<DailyConsumptionItemProjection> findRestOfBoundaryDate(
            Long userId,
            LocalDateTime boundaryStart, LocalDateTime boundaryEnd,
            Long minIncludedId);

    // 지정한 월 범위에서 특정 날짜 이전 데이터가 존재하는지 여부 확인.
    boolean existsOlderThanDate(
            Long userId,
            LocalDateTime monthStart, LocalDateTime monthEnd,
            LocalDateTime boundaryStart);

    // 특정 유저의 날짜별 소비 금액을 문자열 날짜 기준으로 집계하여 반환
    List<DailyAmountProjection> findDailyTotalAmounts(Long userId, LocalDate startDate, LocalDate endDate);

    // 해당 월의 소비 기록을 커서 기반 무한스크롤로 조회
    Slice<DailyConsumptionItemProjection> findMonthlyConsumptionItems(Long userId, LocalDate startDate, LocalDate endDate,
                                                                     Long cursorId, LocalDateTime cursorConsumeDate, int pageSize);

    // 특정 소비 기록 ID에 대한 consumeDate를 조회
    LocalDateTime findConsumeDateById(Long recordId);
}
