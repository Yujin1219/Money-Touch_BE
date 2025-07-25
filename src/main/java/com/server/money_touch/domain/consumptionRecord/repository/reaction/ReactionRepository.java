package com.server.money_touch.domain.consumptionRecord.repository.reaction;

import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.entity.Reaction;
import com.server.money_touch.domain.consumptionRecord.enums.ReactionType;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    
    // 특정 사용자의 특정 소비 기록에 대한 리액션 조회
    Optional<Reaction> findByUserAndConsumptionRecord(User user, ConsumptionRecord consumptionRecord);
    
    // 특정 소비 기록의 특정 타입 리액션 개수 조회
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.consumptionRecord = :consumptionRecord AND r.type = :type")
    Integer countByConsumptionRecordAndType(@Param("consumptionRecord") ConsumptionRecord consumptionRecord, 
                                          @Param("type") ReactionType type);
    
    // 특정 소비 기록의 현명해요 개수 조회
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.consumptionRecord = :consumptionRecord AND r.type = 'WISE'")
    Integer countWiseReactionsByConsumptionRecord(@Param("consumptionRecord") ConsumptionRecord consumptionRecord);
    
    // 특정 소비 기록의 낭비에요 개수 조회
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.consumptionRecord = :consumptionRecord AND r.type = 'WASTE'")
    Integer countWasteReactionsByConsumptionRecord(@Param("consumptionRecord") ConsumptionRecord consumptionRecord);
}