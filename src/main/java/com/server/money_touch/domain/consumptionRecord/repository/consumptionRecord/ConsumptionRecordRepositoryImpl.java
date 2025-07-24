package com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.money_touch.domain.consumptionRecord.entity.QConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.QConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.projection.DailyAmountProjection;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemDetailProjection;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemProjection;
import com.server.money_touch.domain.fixedConsumption.entity.QFixedConsumption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ConsumptionRecordRepositoryImpl implements ConsumptionRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    QConsumptionRecord record = QConsumptionRecord.consumptionRecord;
    QConsumptionCategory category = QConsumptionCategory.consumptionCategory;
    QFixedConsumption fixed = QFixedConsumption.fixedConsumption;

    /**
     * 사용자의 특정 날짜에 해당하는 소비 기록 목록을 조회합니다.
     * - 소비 기록에는 소비 금액, 내용, 카테고리명이 포함됩니다.
     * - 소비 시간(consumeDate) 기준 오름차순(= 먼저 소비된 순서)으로 정렬됩니다.
     *
     * @param userId 사용자 ID
     * @param date 조회할 날짜 (yyyy-MM-dd)
     * @return DailyConsumptionItemProjection 리스트 (DTO 형태)
     */
    @Override
    public List<DailyConsumptionItemDetailProjection> findDailyConsumptionItems(Long userId, LocalDate date) {
        // 입력된 날짜의 시작 시간 (00:00:00)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        log.debug("소비 내역 조회 - 날짜: {}", startOfDay.toLocalDate());

        // 최신순 정렬 + 필요한 컬럼만 조회하여 Projection 반환
        return queryFactory
                .select(Projections.fields(
                        DailyConsumptionItemDetailProjection.class,
                        record.id.as("consumptionRecordId"),
                        Expressions.cases()
                                .when(record.isFixed.isTrue()).then("고정비")
                                .otherwise(category.budgetCategoryName).as("categoryName"),
                        record.content,
                        record.amount
                ))
                .from(record)
                .join(record.consumptionCategory, category)
                .where(
                        record.user.id.eq(userId),
                        record.consumeDate.between(startOfDay, endOfDay)
                )
                .orderBy(record.consumeDate.desc(), record.id.desc()) // 최신순 정렬
                .fetch();
    }


    /**
     * 특정 유저의 날짜별 소비 금액을 문자열 날짜 기준으로 집계하여 반환
     */
    @Override
    public List<DailyAmountProjection> findDailyTotalAmounts(Long userId, LocalDate startDate, LocalDate endDate) {
        // 1. DATE() 함수로 날짜만 추출 (MySQL 인덱스 활용 가능성 ↑)
        Expression<LocalDate> truncatedDate = Expressions.dateTemplate(
                LocalDate.class, "DATE({0})", record.consumeDate
        );

        // 2. QueryDSL로 일별 소비 금액 집계
        return queryFactory
                .select(Projections.fields(
                        DailyAmountProjection.class,
                        Expressions.dateTemplate(java.sql.Date.class, "DATE({0})", record.consumeDate).as("date"),
                        record.amount.sum().as("totalAmount")
                ))
                .from(record)
                .where(
                        record.user.id.eq(userId),
                        record.consumeDate.between(
                                startDate.atStartOfDay(),
                                endDate.atTime(23, 59, 59)
                        )
                )
                .groupBy(Expressions.dateTemplate(java.sql.Date.class, "DATE({0})", record.consumeDate))
                .orderBy(Expressions.dateTemplate(java.sql.Date.class, "DATE({0})", record.consumeDate).asc())
                .fetch();
    }

    // 해당 월의 소비 기록을 커서 기반 무한스크롤로 조회
    @Override
    public Slice<DailyConsumptionItemProjection> findMonthlyConsumptionItems(
            Long userId, LocalDate startDate, LocalDate endDate,
            Long cursorId, LocalDateTime cursorConsumeDate, int pageSize) {

        // 조회 시작일과 종료일을 LocalDateTime으로 변환
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        // 기본 조건: 유저 ID가 일치하고, 소비일이 해당 월 범위 내인 경우
        BooleanExpression baseCondition = record.user.id.eq(userId)
                .and(record.consumeDate.between(start, end));

        // 커서 기반 페이징 조건:
        // - consumeDate가 기준 consumeDate보다 이전이거나
        // - consumeDate가 같으면 ID가 작은 데이터만 조회
        BooleanExpression cursorPredicate = null;
        if (cursorId != null && cursorConsumeDate != null) {
            cursorPredicate = record.consumeDate.lt(cursorConsumeDate)
                    .or(record.consumeDate.eq(cursorConsumeDate).and(record.id.lt(cursorId)));
        }

        // 최종 QueryDSL 쿼리 실행
        List<DailyConsumptionItemProjection> results = queryFactory
                .select(Projections.fields(
                        DailyConsumptionItemProjection.class,
                        record.id.as("consumptionRecordId"),
                        record.consumeDate,
                        Expressions.cases()
                                .when(record.isFixed.isTrue()).then("고정비")
                                .otherwise(category.budgetCategoryName).as("categoryName"),
                        record.content,
                        record.amount
                ))
                .from(record)
                .join(record.consumptionCategory, category)
                .where(baseCondition.and(cursorPredicate)) // 기본 조건 + 커서 조건
                .orderBy(record.consumeDate.desc(), record.id.desc()) // 최신순 정렬
                .limit(pageSize + 1) // 다음 페이지 존재 여부 판단을 위한 +1
                .fetch();

        // Slice 처리를 위한 hasNext 계산
        boolean hasNext = results.size() > pageSize;
        if (hasNext) {
            results.remove(results.size() - 1); // 초과한 1개 제거
        }

        // Pageable은 의미상으로만 사용하므로 offset=0 전달
        return new SliceImpl<>(results, PageRequest.of(0, pageSize), hasNext);
    }

    // 특정 소비 기록 ID에 대한 consumeDate를 조회
    @Override
    public LocalDateTime findConsumeDateById(Long recordId) {
        return queryFactory
                .select(record.consumeDate)
                .from(record)
                .where(record.id.eq(recordId))
                .fetchOne();
    }
}
