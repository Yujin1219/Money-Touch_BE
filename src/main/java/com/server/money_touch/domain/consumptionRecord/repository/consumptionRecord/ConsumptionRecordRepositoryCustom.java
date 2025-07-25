package com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord;

import com.server.money_touch.domain.consumptionRecord.projection.DailyAmountProjection;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemDetailProjection;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ConsumptionRecordRepositoryCustom {
    // 사용자의 특정 날짜에 해당하는 소비 기록 목록을 조회
    List<DailyConsumptionItemDetailProjection> findDailyConsumptionItems(Long userId, LocalDate date);

    // 특정 유저의 날짜별 소비 금액을 문자열 날짜 기준으로 집계하여 반환
    List<DailyAmountProjection> findDailyTotalAmounts(Long userId, LocalDate startDate, LocalDate endDate);

    // 해당 월의 소비 기록을 커서 기반 무한스크롤로 조회
    List<DailyConsumptionItemProjection> findMonthlyConsumptionItems(Long userId, LocalDate startDate, LocalDate endDate,
                                                                     Long cursorId, LocalDateTime cursorConsumeDate, int pageSize);

    // 특정 소비 기록 ID에 대한 consumeDate를 조회
    LocalDateTime findConsumeDateById(Long recordId);
    
    // 공개된 피드 리스트를 커서 기반 무한스크롤로 조회 (N+1 문제 방지를 위한 최적화)
    List<DailyConsumptionItemProjection> findPublicFeedList(Long cursorId, LocalDateTime cursorCreatedAt, int pageSize);
}
