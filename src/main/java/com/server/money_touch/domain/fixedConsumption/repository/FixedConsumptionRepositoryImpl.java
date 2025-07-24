package com.server.money_touch.domain.fixedConsumption.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.money_touch.domain.fixedConsumption.entity.FixedConsumption;
import com.server.money_touch.domain.fixedConsumption.entity.QFixedConsumption;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class FixedConsumptionRepositoryImpl implements FixedConsumptionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    QFixedConsumption fixed = QFixedConsumption.fixedConsumption;

    // 커서 기반 고정비 목록 조회
    @Override
    public Slice<FixedConsumption> findFixedConsumptionsByCursor(Long userId, Long cursorId, Pageable pageable) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(fixed.user.id.eq(userId));
        if (cursorId != null) {
            condition.and(fixed.id.lt(cursorId)); // id 역순으로 작아야 이후 커서
        }

        int pageSize = pageable.getPageSize();
        List<FixedConsumption> results = queryFactory
                .selectFrom(fixed)
                .where(condition)
                .orderBy(fixed.id.desc())
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = results.size() > pageSize;
        if (hasNext) {
            results.remove(pageSize); // Slice에 반환할 범위까지만 유지
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }
}
