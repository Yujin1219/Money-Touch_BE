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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ConsumptionRecordRepositoryImpl implements ConsumptionRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    QConsumptionRecord record = QConsumptionRecord.consumptionRecord;
    QConsumptionCategory category = QConsumptionCategory.consumptionCategory;
    QFixedConsumption fixed = QFixedConsumption.fixedConsumption;

    // 사용자의 특정 날짜에 해당하는 소비 기록 목록을 조회
    @Override
    public Slice<DailyConsumptionItemDetailProjection> findDailyConsumptionItemsWithCursor(
            Long userId,
            LocalDateTime start,
            LocalDateTime end,
            Long cursorId,
            LocalDateTime cursorConsumeDate,
            int pageSize
    ) {
        // 기본 조건
        BooleanExpression base = record.user.id.eq(userId)
                .and(record.consumeDate.between(start, end));

        // 1) 이번 페이지의 대표 날짜 찾기
        BooleanExpression dateCursor = null;
        if (cursorConsumeDate != null) {
            LocalDateTime cursorDayStart = cursorConsumeDate.toLocalDate().atStartOfDay();
            dateCursor = record.consumeDate.lt(cursorDayStart);
        }

        DailyConsumptionItemDetailProjection top = queryFactory
                .select(Projections.fields(
                        DailyConsumptionItemDetailProjection.class,
                        record.id.as("consumptionRecordId"),
                        record.consumeDate.as("consumeDate")
                ))
                .from(record)
                .where(andAll(base, dateCursor))
                .orderBy(record.consumeDate.desc(), record.id.desc())
                .limit(1)
                .fetchOne();

        if (top == null) {
            return new SliceImpl<>(List.of(), PageRequest.of(0, 1), false);
        }

        // 2) 대표 날짜 하루 범위
        LocalDate targetDate = top.getConsumeDate().toLocalDate();
        LocalDateTime dayStart = targetDate.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1).minusNanos(1);

        // 3) 같은 날짜 전부 조회 (limit 없음)
        List<DailyConsumptionItemDetailProjection> itemsOfTheDay = queryFactory
                .select(Projections.fields(
                        DailyConsumptionItemDetailProjection.class,
                        record.id.as("consumptionRecordId"),
                        Expressions.cases()
                                .when(record.isFixed.isTrue()).then("고정비")
                                .otherwise(category.budgetCategoryName).as("categoryName"),
                        record.content,
                        record.amount,
                        record.consumeDate.as("consumeDate")
                ))
                .from(record)
                .join(record.consumptionCategory, category)
                .where(base.and(record.consumeDate.between(dayStart, dayEnd)))
                .orderBy(record.consumeDate.desc(), record.id.desc())
                .fetch();

        // 4) 다음 날짜가 존재하는지 확인
        boolean hasNext = queryFactory
                .select(record.id)
                .from(record)
                .where(base.and(record.consumeDate.lt(dayStart)))
                .limit(1)
                .fetchFirst() != null;

        // SliceImpl 생성 시 페이지 크기를 같은 날짜의 개수로 맞춤
        return new SliceImpl<>(itemsOfTheDay, PageRequest.of(0, itemsOfTheDay.size()), hasNext);
    }

    /**
     * 해당 월의 소비내역을 커서 기반으로 페이지 단위 조회 (consumeDate 기준).
     *
     * - 기본적으로 consumeDate(소비일자) + id 내림차순으로 정렬
     * - cursorConsumeDate가 지정되면 해당 날짜의 0시 이전 데이터만 조회
     *   → 이렇게 하면 동일한 날짜의 데이터는 한 페이지에 모두 포함됨
     *
     * @param userId             조회할 사용자 ID
     * @param monthStart         월 시작일(LocalDateTime, 00:00)
     * @param monthEnd           월 종료일(LocalDateTime, 23:59:59)
     * @param cursorConsumeDate  커서로 사용할 기준 소비일시 (null이면 첫 페이지)
     * @param limit              조회할 데이터 개수(페이지 사이즈)
     * @return DailyConsumptionItemProjection 목록
     */
    @Override
    public List<DailyConsumptionItemProjection> findChunkByMonthUsingDateCursor(
            Long userId, LocalDateTime monthStart, LocalDateTime monthEnd,
            LocalDateTime cursorConsumeDate, int limit) {

        // 기본 조건: 해당 사용자 + 지정한 월 범위 내 소비내역
        BooleanExpression base = record.user.id.eq(userId)
                .and(record.consumeDate.between(monthStart, monthEnd));

        // 날짜 커서 조건: 다음 페이지는 cursorConsumeDate의 '자정'보다 이전 데이터만 조회
        BooleanExpression dateCursor = null;
        if (cursorConsumeDate != null) {
            LocalDateTime cursorDayStart = cursorConsumeDate.toLocalDate().atStartOfDay();
            dateCursor = record.consumeDate.lt(cursorDayStart);
        }

        return queryFactory
                .select(Projections.fields(
                        DailyConsumptionItemProjection.class,
                        record.id.as("consumptionRecordId"),
                        record.consumeDate.as("consumeDate"),
                        Expressions.cases()
                                .when(record.isFixed.isTrue()).then("고정비")
                                .otherwise(category.budgetCategoryName).as("categoryName"),
                        record.content,
                        record.amount
                ))
                .from(record)
                .leftJoin(record.consumptionCategory, category)
                .where(andAll(base, dateCursor))
                .orderBy(record.consumeDate.desc(), record.id.desc()) // 최신순
                .limit(limit)
                .fetch();
    }

    /**
     * 경계 날짜(boundary date)의 나머지 데이터를 추가 조회.
     *
     * - 현재 페이지에서 경계 날짜의 일부 데이터만 포함된 경우,
     *   같은 날짜의 나머지 데이터를 모두 가져오기 위해 호출됨
     * - minIncludedId보다 오래된(더 작은) ID 데이터만 조회하여 중복 방지
     *
     * @param userId        조회할 사용자 ID
     * @param boundaryStart 경계 날짜의 시작 시간 (00:00)
     * @param boundaryEnd   경계 날짜의 종료 시간 (23:59:59)
     * @param minIncludedId 현재 페이지에서 이미 포함된 가장 오래된 데이터의 ID
     * @return DailyConsumptionItemProjection 목록
     */
    @Override
    public List<DailyConsumptionItemProjection> findRestOfBoundaryDate(
            Long userId, LocalDateTime boundaryStart, LocalDateTime boundaryEnd, Long minIncludedId) {

        return queryFactory
                .select(Projections.fields(
                        DailyConsumptionItemProjection.class,
                        record.id.as("consumptionRecordId"),
                        record.consumeDate.as("consumeDate"),
                        Expressions.cases()
                                .when(record.isFixed.isTrue()).then("고정비")
                                .otherwise(category.budgetCategoryName).as("categoryName"),
                        record.content,
                        record.amount
                ))
                .from(record)
                .leftJoin(record.consumptionCategory, category)
                .where(
                        record.user.id.eq(userId)
                                .and(record.consumeDate.between(boundaryStart, boundaryEnd))
                                .and(record.id.lt(minIncludedId))
                )
                .orderBy(record.consumeDate.desc(), record.id.desc())
                .fetch();
    }

    /**
     * 지정한 월 범위에서 특정 날짜 이전 데이터가 존재하는지 여부 확인.
     *
     * - 다음 페이지가 있는지 여부를 판단하는 데 사용
     * - boundaryStart 이전에 데이터가 존재하면 true 반환
     *
     * @param userId        조회할 사용자 ID
     * @param monthStart    월 시작일(LocalDateTime, 00:00)
     * @param monthEnd      월 종료일(LocalDateTime, 23:59:59)
     * @param boundaryStart 비교 기준이 되는 날짜(LocalDateTime, 00:00)
     * @return 존재 여부 (true: 데이터 있음, false: 데이터 없음)
     */
    @Override
    public boolean existsOlderThanDate(
            Long userId, LocalDateTime monthStart, LocalDateTime monthEnd, LocalDateTime boundaryStart) {

        Long probe = queryFactory
                .select(record.id)
                .from(record)
                .where(
                        record.user.id.eq(userId)
                                .and(record.consumeDate.between(monthStart, monthEnd))
                                .and(record.consumeDate.lt(boundaryStart))
                )
                .limit(1)
                .fetchFirst();

        return probe != null;
    }

    /**
     * BooleanExpression들을 안전하게 AND 연산으로 결합하는 유틸 메서드.
     * null 조건은 무시함.
     *
     * @param xs 조건 배열
     * @return 결합된 BooleanExpression
     */
    private static BooleanExpression andAll(BooleanExpression... xs) {
        BooleanExpression acc = null;
        for (BooleanExpression x : xs) {
            if (x == null) continue;
            acc = (acc == null) ? x : acc.and(x);
        }
        return acc;
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
