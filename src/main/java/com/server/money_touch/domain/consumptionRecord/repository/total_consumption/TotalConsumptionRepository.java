package com.server.money_touch.domain.consumptionRecord.repository.total_consumption;

import com.server.money_touch.domain.consumptionRecord.entity.TotalConsumption;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TotalConsumptionRepository extends JpaRepository<TotalConsumption, Long> {
    Optional<TotalConsumption> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
}
