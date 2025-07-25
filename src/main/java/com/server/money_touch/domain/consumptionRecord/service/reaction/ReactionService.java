package com.server.money_touch.domain.consumptionRecord.service.reaction;

import com.server.money_touch.domain.consumptionRecord.dto.FeedRequest;
import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;

public interface ReactionService {
    
    /**
     * 리액션 추가/변경/삭제
     * @param userId 사용자 ID
     * @param consumptionRecordId 소비 기록 ID
     * @param request 리액션 요청 정보
     * @return 리액션 결과 정보
     */
    FeedResponse.ReactionResultDTO addOrUpdateReaction(Long userId, Long consumptionRecordId, FeedRequest.ReactionCreateDTO request);
    
    /**
     * 특정 소비 기록의 리액션 통계 조회
     * @param consumptionRecordId 소비 기록 ID
     * @param userId 현재 사용자 ID (내 리액션 확인용)
     * @return 리액션 통계 정보
     */
    FeedResponse.ReactionResultDTO getReactionStats(Long consumptionRecordId, Long userId);
}