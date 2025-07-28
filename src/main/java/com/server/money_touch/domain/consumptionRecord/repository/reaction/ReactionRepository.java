package com.server.money_touch.domain.consumptionRecord.repository.reaction;

import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.entity.Reaction;
import com.server.money_touch.domain.consumptionRecord.enums.ReactionType;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    // 특정 사용자의 특정 소비기록에 대한 리액션 조회
    Optional<Reaction> findByUserAndConsumptionRecord(User user, ConsumptionRecord consumptionRecord);

    // 특정 사용자의 여러 소비기록에 대한 리액션 조회 (피드 리스트 조회시 N + 1 문제 해결) - 공개 피드만 대상으로 함
    @Query("SELECT r FROM Reaction r " +
            "WHERE r.user = :user " +
            "AND r.consumptionRecord.id IN :consumptionRecordIds " +
            "AND r.consumptionRecord.isPublic = true")
    List<Reaction> findByUserAndPublicConsumptionRecordIds(@Param("user") User user,
                                                           @Param("consumptionRecordIds") List<Long> consumptionRecordIds);


    // 특정 소비기록에 대한 리액션 수 조회 (WISE 또는 WASTE)
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.consumptionRecord.id = :consumptionRecordId AND r.type = :type")
    Integer countReactionByConsumptionRecordIdAndType(@Param("consumptionRecordId") Long consumptionRecordId,
                                                      @Param("type") ReactionType type);

}
