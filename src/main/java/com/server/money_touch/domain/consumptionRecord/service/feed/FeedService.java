package com.server.money_touch.domain.consumptionRecord.service.feed;

import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.enums.FeedSortType;

public interface FeedService {

    // 피드 상세 조회
    FeedResponse.FeedDetailResultDTO getFeedDetail(Long userId, Long consumptionRecordId);

    // 피드 조회수 증가
    FeedResponse.ViewCountResultDTO increaseFeedViewCount(Long userId, Long consumptionRecordId);

    // 피드 리스트 조회
    FeedResponse.FeedListResultDTO getFeedsByCursor(Long userId, FeedSortType sortType, Long cursorId, Integer cursorViewCount);
}
