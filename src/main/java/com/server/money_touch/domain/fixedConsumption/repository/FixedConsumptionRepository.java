package com.server.money_touch.domain.fixedConsumption.repository;

import com.server.money_touch.domain.fixedConsumption.entity.FixedConsumption;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixedConsumptionRepository extends JpaRepository<FixedConsumption, Long>, FixedConsumptionRepositoryCustom {
    List<FixedConsumption> findAllByUser(User user);
}