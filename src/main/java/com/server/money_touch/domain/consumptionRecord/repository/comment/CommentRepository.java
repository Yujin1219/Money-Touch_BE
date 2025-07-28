package com.server.money_touch.domain.consumptionRecord.repository.comment;

import com.server.money_touch.domain.consumptionRecord.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
