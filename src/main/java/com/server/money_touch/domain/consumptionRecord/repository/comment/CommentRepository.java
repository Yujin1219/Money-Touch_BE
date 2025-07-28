package com.server.money_touch.domain.consumptionRecord.repository.comment;

import com.server.money_touch.domain.consumptionRecord.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 대댓글 포함 모든 댓글 수 count
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.consumptionRecord.id = :recordId")
    int countAllByConsumptionRecordId(@Param("recordId") Long recordId);

}
