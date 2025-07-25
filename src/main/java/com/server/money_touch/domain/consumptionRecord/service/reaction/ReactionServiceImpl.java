package com.server.money_touch.domain.consumptionRecord.service.reaction;

import com.server.money_touch.domain.consumptionRecord.dto.FeedRequest;
import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.entity.Reaction;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.consumptionRecord.repository.reaction.ReactionRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final ConsumptionRecordRepository consumptionRecordRepository;

    @Override
    @Transactional
    public FeedResponse.ReactionResultDTO addOrUpdateReaction(Long userId, Long consumptionRecordId, FeedRequest.ReactionCreateDTO request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 소비 기록 조회
        ConsumptionRecord consumptionRecord = consumptionRecordRepository.findById(consumptionRecordId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND));

        // 기존 리액션 조회
        Optional<Reaction> existingReaction = reactionRepository.findByUserAndConsumptionRecord(user, consumptionRecord);

        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            
            // 같은 타입의 리액션을 다시 누르면 삭제 (취소)
            if (reaction.getType() == request.getType()) {
                reactionRepository.delete(reaction);
                log.info("리액션 삭제 - 사용자 ID: {}, 소비 기록 ID: {}, 타입: {}", userId, consumptionRecordId, request.getType());
            } else {
                // 다른 타입의 리액션으로 변경
                reaction.updateType(request.getType());
                log.info("리액션 변경 - 사용자 ID: {}, 소비 기록 ID: {}, 기존 타입: {} -> 새 타입: {}", 
                        userId, consumptionRecordId, reaction.getType(), request.getType());
            }
        } else {
            // 새로운 리액션 생성
            Reaction newReaction = Reaction.builder()
                    .user(user)
                    .consumptionRecord(consumptionRecord)
                    .type(request.getType())
                    .build();
            
            reactionRepository.save(newReaction);
            log.info("새 리액션 생성 - 사용자 ID: {}, 소비 기록 ID: {}, 타입: {}", userId, consumptionRecordId, request.getType());
        }

        // 리액션 통계 조회 및 반환
        return getReactionStats(consumptionRecordId, userId);
    }

    @Override
    public FeedResponse.ReactionResultDTO getReactionStats(Long consumptionRecordId, Long userId) {
        // 소비 기록 조회
        ConsumptionRecord consumptionRecord = consumptionRecordRepository.findById(consumptionRecordId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND));

        // 리액션 개수 조회
        Integer wiseCount = reactionRepository.countWiseReactionsByConsumptionRecord(consumptionRecord);
        Integer wasteCount = reactionRepository.countWasteReactionsByConsumptionRecord(consumptionRecord);

        // 현재 사용자의 리액션 조회
        String myReaction = null;
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
            
            Optional<Reaction> userReaction = reactionRepository.findByUserAndConsumptionRecord(user, consumptionRecord);
            if (userReaction.isPresent()) {
                myReaction = userReaction.get().getType().name();
            }
        }

        return FeedResponse.ReactionResultDTO.builder()
                .consumptionRecordId(consumptionRecordId)
                .wiseCount(wiseCount)
                .wasteCount(wasteCount)
                .myReaction(myReaction)
                .build();
    }
}