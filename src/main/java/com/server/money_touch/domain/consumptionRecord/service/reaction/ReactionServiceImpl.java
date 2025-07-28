package com.server.money_touch.domain.consumptionRecord.service.reaction;

import com.server.money_touch.domain.consumptionRecord.converter.reaction.ReactionConverter;
import com.server.money_touch.domain.consumptionRecord.dto.FeedRequest;
import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.entity.Reaction;
import com.server.money_touch.domain.consumptionRecord.enums.ReactionType;
import com.server.money_touch.domain.consumptionRecord.repository.reaction.ReactionRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final ConsumptionRecordRepository consumptionRecordRepository;

    @Transactional
    @Override
    public FeedResponse.ReactionResultDTO addOrUpdateReaction(Long userId, Long consumptionRecordId, FeedRequest.ReactionCreateDTO request) {

        // 1. 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 소비기록 조회
        ConsumptionRecord consumptionRecord = consumptionRecordRepository.findById(consumptionRecordId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND));


        // 3. 공개 피드만 리액션 가능
        if (!consumptionRecord.isPublic()) {
            throw new ErrorHandler(ErrorStatus.FORBIDDEN_REACTION_ON_PRIVATE_FEED);
        }

        // 4. 기존 리액션 존재 여부 조회
        Optional<Reaction> existingReaction = reactionRepository.findByUserAndConsumptionRecord(user, consumptionRecord);
        String myReaction = null;

        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();

            // 5-1. 같은 타입 누르면 → 삭제
            if (reaction.getType() == request.getType()) {
                reactionRepository.delete(reaction);
            } else {
                // 5-2. 다른 타입 누르면 → 변경
                reaction.updateType(request.getType());
                myReaction = request.getType().name();
            }
        } else {
            // 5-3. 리액션 처음 누름 → 새로 등록
            Reaction newReaction = Reaction.builder()
                    .user(user)
                    .consumptionRecord(consumptionRecord)
                    .type(request.getType())
                    .build();
            reactionRepository.save(newReaction);
            myReaction = request.getType().name();
        }

        // 6. 변경된 리액션 카운트 조회
        Integer wiseCount = reactionRepository.countReactionByConsumptionRecordIdAndType(consumptionRecordId, ReactionType.WISE);
        Integer wasteCount = reactionRepository.countReactionByConsumptionRecordIdAndType(consumptionRecordId, ReactionType.WASTE);

        // 7. ConsumptionRecord에 반영
        consumptionRecord.setWiseCount(wiseCount);
        consumptionRecord.setWasteCount(wasteCount);
        consumptionRecordRepository.save(consumptionRecord);

        // 8. 결과 DTO 생성
        return ReactionConverter.toReactionResultDTO(consumptionRecordId, wiseCount, wasteCount, myReaction);
    }
}
