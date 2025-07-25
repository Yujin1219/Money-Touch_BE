package com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory;

import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConsumptionCategoryRepository extends JpaRepository<ConsumptionCategory, Long> {
    // 카테고리 이름과 타입으로 유저의 소비 카테고리 조회
    Optional<ConsumptionCategory> findByUserAndBudgetCategoryNameAndBudgetCategoryType(User user, String budgetCategoryName, CategoryType type);

    List<ConsumptionCategory> findAllByUser(User user);

    // 카테고리 이름으로 유저의 소비 카테고리 조회
    Optional<ConsumptionCategory> findByUserAndBudgetCategoryName(User user, String budgetCategoryName);

    // 소비 기록과 연관된 소비 카테고리 조회
    @Query("SELECT cc FROM ConsumptionRecord cr " +
            "JOIN cr.consumptionCategory cc " +
            "WHERE cr.id = :consumptionRecordId")
    Optional<ConsumptionCategory> findCategoryByConsumptionRecordId(@Param("consumptionRecordId") Long consumptionRecordId);

    // 요청한 이름 목록에 해당하는 소비 카테고리를 userId 기준으로 조회
    @Query("SELECT c FROM ConsumptionCategory c WHERE c.user.id = :userId AND c.budgetCategoryName IN :names")
    List<ConsumptionCategory> findAllByUserIdAndNames(@Param("userId") Long userId, @Param("names") List<String> names);

    List<ConsumptionCategory> findAllByUserAndBudgetCategoryType(User user, CategoryType type);
}