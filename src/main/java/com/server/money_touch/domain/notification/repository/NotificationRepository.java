package com.server.money_touch.domain.notification.repository;

import com.server.money_touch.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 커서 기반 무한스크롤로 알림 조회
     * cursorId가 null이면 첫 페이지 조회 (모든 알림)
     * cursorId가 있으면 해당 ID보다 작은 알림들 조회
     * ID 기준 내림차순 정렬 (최신순)
     */
    @Query("SELECT n FROM Notification n " +
            "WHERE n.user.id = :userId " +
            "AND (:cursorId IS NULL OR n.id < :cursorId) " +
            "ORDER BY n.id DESC")
    Slice<Notification> findNotificationsByCursor(
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

}
