package com.server.money_touch.domain.consumptionRecord.repository.feed;

import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FeedRepository extends JpaRepository<ConsumptionRecord, Long> {

    // 피드 상세 조회를 위한 소비기록 + 유저 + 카테고리 + 이미지 fetch join
    @EntityGraph(attributePaths = {"user", "consumptionCategory", "images"})
    Optional<ConsumptionRecord> findWithAllById(Long id);

    // 조회수 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ConsumptionRecord c SET c.viewCount = c.viewCount + 1 WHERE c.id = :id AND c.isPublic = true")
    void incrementViewCountIfPublic(@Param("id") Long id);

    // 최신순 정렬 (ID 기준 내림차순)
    @Query("SELECT r FROM ConsumptionRecord r " +
            "WHERE r.isPublic = true " +
            "AND (:cursorId IS NULL OR r.id < :cursorId) " +
            "ORDER BY r.id DESC")
    Slice<ConsumptionRecord> findByCursorOrderByIdDesc(
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // 조회수순 정렬 (viewCount 기준 내림차순, 같을 경우 id 기준 내림차순)
    @Query("SELECT r FROM ConsumptionRecord r " +
            "WHERE r.isPublic = true " +
            "AND (:cursorViewCount IS NULL OR " +
            "      (r.viewCount < :cursorViewCount OR (r.viewCount = :cursorViewCount AND r.id < :cursorId))) " +
            "ORDER BY r.viewCount DESC, r.id DESC")
    Slice<ConsumptionRecord> findByCursorOrderByViewCountDesc(
            @Param("cursorViewCount") Integer cursorViewCount,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
