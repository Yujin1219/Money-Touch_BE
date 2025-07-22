package com.server.money_touch.domain.home.repository;

import com.server.money_touch.domain.home.entity.WiseRankingHistory;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WiseRankingHistoryRepository extends JpaRepository<WiseRankingHistory,Long> {

    Optional<WiseRankingHistory> findByUserAndRankingWeekStartDate(User user, LocalDate rankingWeekStartDate);

    List<WiseRankingHistory> findAllByRankingWeekStartDateOrderByRankingAsc(LocalDate weekStartDate);

}
