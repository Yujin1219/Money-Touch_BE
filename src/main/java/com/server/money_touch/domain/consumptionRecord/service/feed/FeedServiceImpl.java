package com.server.money_touch.domain.consumptionRecord.service.feed;

import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.entity.Reaction;
import com.server.money_touch.domain.consumptionRecord.enums.ReactionType;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemProjection;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.consumptionRecord.repository.reaction.ReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

    private final ConsumptionRecordRepository consumptionRecordRepository;
    private final ReactionRepository reactionRepository;

    @Override
    public FeedResponse.OptimizedFeedListResultDTO getPublicFeedList(Long userId, Long cursorId, LocalDateTime cursorCreatedAt, int pageSize) {
        // 1. 피드 리스트 조회 (기본 정보만)
        List<DailyConsumptionItemProjection> feedItems = consumptionRecordRepository
                .findPublicFeedList(cursorId, cursorCreatedAt, pageSize);

        // 빈 결과 처리
        if (feedItems.isEmpty()) {
            return FeedResponse.OptimizedFeedListResultDTO.builder()
                    .feedList(Collections.emptyList())
                    .feedListSize(0)
                    .isFirst(cursorId == null)
                    .isLast(true)
                    .hasNext(false)
                    .build();
        }

        // 2. 페이징 처리
        boolean hasNext = feedItems.size() > pageSize;
        if (hasNext) {
            feedItems = feedItems.subList(0, pageSize); // 마지막 아이템 제거
        }

        // 3. 소비 기록 ID 목록 추출
        List<Long> consumptionRecordIds = feedItems.stream()
                .map(DailyConsumptionItemProjection::getConsumptionRecordId)
                .collect(Collectors.toList());

        // 4. 리액션 통계 배치 조회 (N+1 문제 해결)
        Map<Long, ReactionStats> reactionStatsMap = getReactionStatsMap(consumptionRecordIds);

        // 5. 현재 사용자의 리액션 배치 조회 (N+1 문제 해결)
        Map<Long, String> userReactionMap = getUserReactionMap(userId, consumptionRecordIds);

        // 6. 최종 DTO 변환
        List<FeedResponse.OptimizedFeedItemDTO> optimizedFeedList = feedItems.stream()
                .map(item -> convertToOptimizedFeedItem(item, reactionStatsMap, userReactionMap))
                .collect(Collectors.toList());

        // 7. 다음 커서 정보 설정
        Long nextCursorId = null;
        LocalDateTime nextCursorCreatedAt = null;
        if (hasNext && !feedItems.isEmpty()) {
            DailyConsumptionItemProjection lastItem = feedItems.get(feedItems.size() - 1);
            nextCursorId = lastItem.getConsumptionRecordId();
            nextCursorCreatedAt = lastItem.getConsumeDate(); // 실제로는 createdAt
        }

        return FeedResponse.OptimizedFeedListResultDTO.builder()
                .feedList(optimizedFeedList)
                .feedListSize(optimizedFeedList.size())
                .isFirst(cursorId == null)
                .isLast(!hasNext)
                .hasNext(hasNext)
                .nextCursorId(nextCursorId)
                .nextCursorCreatedAt(nextCursorCreatedAt)
                .build();
    }

    /**
     * 여러 소비 기록의 리액션 통계를 한 번에 조회하여 Map으로 반환
     */
    private Map<Long, ReactionStats> getReactionStatsMap(List<Long> consumptionRecordIds) {
        if (consumptionRecordIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ReactionRepository.ReactionStatsProjection> statsProjections = 
                reactionRepository.findReactionStatsByConsumptionRecordIds(consumptionRecordIds);

        Map<Long, ReactionStats> statsMap = new HashMap<>();
        
        // 모든 소비 기록에 대해 기본값 설정
        for (Long recordId : consumptionRecordIds) {
            statsMap.put(recordId, new ReactionStats(0, 0));
        }

        // 실제 통계 데이터로 업데이트
        for (ReactionRepository.ReactionStatsProjection projection : statsProjections) {
            Long recordId = projection.getConsumptionRecordId();
            ReactionType type = projection.getReactionType();
            Long count = projection.getCount();

            ReactionStats stats = statsMap.get(recordId);
            if (type == ReactionType.WISE) {
                stats.setWiseCount(count.intValue());
            } else if (type == ReactionType.WASTE) {
                stats.setWasteCount(count.intValue());
            }
        }

        return statsMap;
    }

    /**
     * 특정 사용자의 여러 소비 기록에 대한 리액션을 한 번에 조회하여 Map으로 반환
     */
    private Map<Long, String> getUserReactionMap(Long userId, List<Long> consumptionRecordIds) {
        if (userId == null || consumptionRecordIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Reaction> userReactions = reactionRepository
                .findByUserIdAndConsumptionRecordIds(userId, consumptionRecordIds);

        return userReactions.stream()
                .collect(Collectors.toMap(
                        reaction -> reaction.getConsumptionRecord().getId(),
                        reaction -> reaction.getType().name()
                ));
    }

    /**
     * DailyConsumptionItemProjection을 OptimizedFeedItemDTO로 변환
     */
    private FeedResponse.OptimizedFeedItemDTO convertToOptimizedFeedItem(
            DailyConsumptionItemProjection item,
            Map<Long, ReactionStats> reactionStatsMap,
            Map<Long, String> userReactionMap) {

        Long recordId = item.getConsumptionRecordId();
        ReactionStats stats = reactionStatsMap.getOrDefault(recordId, new ReactionStats(0, 0));
        String myReaction = userReactionMap.get(recordId);

        return FeedResponse.OptimizedFeedItemDTO.builder()
                .consumptionRecordId(recordId)
                .amount(item.getAmount())
                .content(item.getContent())
                .createdAt(item.getConsumeDate()) // 실제로는 createdAt
                .wiseCount(stats.getWiseCount())
                .wasteCount(stats.getWasteCount())
                .myReaction(myReaction)
                // TODO: 추후 필요시 user, category, imageUrl, memo, commentCount, viewCount 추가
                .commentCount(0) // 임시값
                .viewCount(0) // 임시값
                .build();
    }

    /**
     * 리액션 통계를 담는 내부 클래스
     */
    private static class ReactionStats {
        private int wiseCount;
        private int wasteCount;

        public ReactionStats(int wiseCount, int wasteCount) {
            this.wiseCount = wiseCount;
            this.wasteCount = wasteCount;
        }

        public int getWiseCount() {
            return wiseCount;
        }

        public void setWiseCount(int wiseCount) {
            this.wiseCount = wiseCount;
        }

        public int getWasteCount() {
            return wasteCount;
        }

        public void setWasteCount(int wasteCount) {
            this.wasteCount = wasteCount;
        }
    }
}