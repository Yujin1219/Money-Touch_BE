package com.server.money_touch.domain.consumptionRecord.service.feed;

import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;

import java.time.LocalDateTime;

public interface FeedService {
    
    /**
     * 공개된 피드 리스트를 커서 기반 무한스크롤로 조회 (N+1 문제 최적화)
     * @param userId 현재 사용자 ID (리액션 정보 조회용, null 가능)
     * @param cursorId 커서 ID (첫 페이지는 null)
     * @param cursorCreatedAt 커서 생성일시 (첫 페이지는 null)
     * @param pageSize 페이지 크기
     * @return 최적화된 피드 리스트
     */
    FeedResponse.OptimizedFeedListResultDTO getPublicFeedList(Long userId, Long cursorId, LocalDateTime cursorCreatedAt, int pageSize);
}