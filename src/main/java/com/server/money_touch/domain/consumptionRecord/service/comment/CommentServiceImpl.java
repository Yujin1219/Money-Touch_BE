package com.server.money_touch.domain.consumptionRecord.service.comment;

import com.server.money_touch.domain.consumptionRecord.converter.comment.CommentConverter;
import com.server.money_touch.domain.consumptionRecord.dto.FeedRequest;
import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.entity.Comment;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.repository.comment.CommentRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ConsumptionRecordRepository consumptionRecordRepository;

    @Override
    @Transactional
    public FeedResponse.CommentResultDTO createComment(Long userId, Long consumptionRecordId, FeedRequest.CommentCreateDTO request) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 소비기록 조회
        ConsumptionRecord consumptionRecord = consumptionRecordRepository.findById(consumptionRecordId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND));

        // 3. 소비기록이 공개된 경우에만 댓글 작성 가능
        if (!consumptionRecord.isPublic()) {
            throw new ErrorHandler(ErrorStatus.FORBIDDEN_ACCESS_ON_PRIVATE_FEED);
        }

        // 4. 부모 댓글(대댓글) 처리
        Comment parent = null;
        if (request.getParentId() != null) {
            // 4-1. 부모 댓글 존재 여부 확인
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ErrorHandler(ErrorStatus.PARENT_COMMENT_NOT_FOUND));

            // 4-2. 부모 댓글이 이미 대댓글이라면, 2단계 이상의 중첩은 금지
            if (parent.getParent() != null) {
                throw new ErrorHandler(ErrorStatus.NESTED_REPLY_NOT_ALLOWED);
            }
        }

        // 5. 댓글 엔티티 생성
        Comment comment = CommentConverter.toComment(request, user, consumptionRecord);

        // 6. 대댓글이라면 부모 설정
        if (parent != null) {
            comment.setParent(parent);
        }

        // 7. 댓글 저장
        commentRepository.save(comment);

        // 8. 전체 댓글 수 재계산 (부모 댓글 + 대댓글 포함)
        int totalCommentCount = commentRepository.countAllByConsumptionRecordId(consumptionRecordId);

        // 9. 소비기록에 반영
        consumptionRecord.setCommentCount(totalCommentCount);
        consumptionRecordRepository.save(consumptionRecord);

        // 10. 응답 DTO 반환
        return FeedResponse.CommentResultDTO.builder()
                .commentId(comment.getId())
                .build();
    }
}
