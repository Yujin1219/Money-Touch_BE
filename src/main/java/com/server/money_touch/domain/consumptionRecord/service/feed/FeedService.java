package com.server.money_touch.domain.consumptionRecord.service.feed;

import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.enums.FeedSortType;
import com.server.money_touch.domain.consumptionRecord.enums.MyFeedViewType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface FeedService {

    // 피드 상세 조회
    FeedResponse.FeedDetailResultDTO getFeedDetail(Long userId, Long consumptionRecordId, HttpServletRequest request, HttpServletResponse response);

    // 피드 리스트 조회
    FeedResponse.FeedListResultDTO getFeedsByCursor(Long userId, FeedSortType sortType, Long cursorId, Integer cursorViewCount);

    // 유저명으로 피드 검색
    FeedResponse.FeedListResultDTO searchFeedsByUserNickname(String keyword, Long cursorId, Long userId);

    // 나의 피드 리스트 조회
    FeedResponse.MyFeedListResultDTO getMyFeedsByCursor(Long userId, MyFeedViewType viewMode, Long cursorId);
}
