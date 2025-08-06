package com.server.money_touch.domain.consumptionMbti.repository;

import com.server.money_touch.domain.consumptionMbti.entity.ConsumptionMbti;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ConsumptionMbtiRepository extends CrudRepository<ConsumptionMbti, Long> {
    // 소비 MBTI 존재 유무 확인
    Optional <ConsumptionMbti> findByResult(String result);
}
