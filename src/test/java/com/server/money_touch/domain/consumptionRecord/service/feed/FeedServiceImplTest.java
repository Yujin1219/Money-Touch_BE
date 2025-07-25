package com.server.money_touch.domain.consumptionRecord.service.feed;

import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.entity.Reaction;
import com.server.money_touch.domain.consumptionRecord.enums.ReactionType;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemProjection;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.consumptionRecord.repository.reaction.ReactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceImplTest {

    @Mock
    private ConsumptionRecordRepository consumptionRecordRepository;

    @Mock
    private ReactionRepository reactionRepository;

    @InjectMocks
    private FeedServiceImpl feedService;

    @Test
    @DisplayName("최적화된 피드 리스트 조회 성공 - N+1 문제 해결")
    void getPublicFeedList_Success() {
        // given
        Long userId = 1L;
        Long cursorId = null;
        LocalDateTime cursorCreatedAt = null;
        int pageSize = 20;

        // Mock 피드 아이템 생성
        DailyConsumptionItemProjection item1 = mock(DailyConsumptionItemProjection.class);
        given(item1.getConsumptionRecordId()).willReturn(1L);
        given(item1.getAmount()).willReturn(10000);
        given(item1.getContent()).willReturn("스타벅스 아메리카노");
        given(item1.getConsumeDate()).willReturn(LocalDateTime.of(2024, 3, 15, 14, 30));

        List<DailyConsumptionItemProjection> mockFeedItems = Arrays.asList(item1);

        // Mock 리액션 통계 생성
        ReactionRepository.ReactionStatsProjection stats1 = mock(ReactionRepository.ReactionStatsProjection.class);
        given(stats1.getConsumptionRecordId()).willReturn(1L);
        given(stats1.getReactionType()).willReturn(ReactionType.WISE);
        given(stats1.getCount()).willReturn(3L);
        List<ReactionRepository.ReactionStatsProjection> mockReactionStats = Arrays.asList(stats1);

        // Mock 사용자 리액션 생성
        Reaction userReaction = mock(Reaction.class);
        com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord consumptionRecord = 
                mock(com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord.class);
        given(consumptionRecord.getId()).willReturn(1L);
        given(userReaction.getConsumptionRecord()).willReturn(consumptionRecord);
        given(userReaction.getType()).willReturn(ReactionType.WISE);
        List<Reaction> mockUserReactions = Arrays.asList(userReaction);

        // Mock 설정
        given(consumptionRecordRepository.findPublicFeedList(cursorId, cursorCreatedAt, pageSize))
                .willReturn(mockFeedItems);
        given(reactionRepository.findReactionStatsByConsumptionRecordIds(eq(Arrays.asList(1L))))
                .willReturn(mockReactionStats);
        given(reactionRepository.findByUserIdAndConsumptionRecordIds(eq(userId), eq(Arrays.asList(1L))))
                .willReturn(mockUserReactions);

        // when
        FeedResponse.OptimizedFeedListResultDTO result = feedService.getPublicFeedList(userId, cursorId, cursorCreatedAt, pageSize);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFeedList()).hasSize(1);
        assertThat(result.getFeedListSize()).isEqualTo(1);
        assertThat(result.getIsFirst()).isTrue();
        assertThat(result.getIsLast()).isTrue();
        assertThat(result.getHasNext()).isFalse();

        // 첫 번째 피드 아이템 검증
        FeedResponse.OptimizedFeedItemDTO firstItem = result.getFeedList().get(0);
        assertThat(firstItem.getConsumptionRecordId()).isEqualTo(1L);
        assertThat(firstItem.getAmount()).isEqualTo(10000);
        assertThat(firstItem.getContent()).isEqualTo("스타벅스 아메리카노");
        assertThat(firstItem.getWiseCount()).isEqualTo(3);
        assertThat(firstItem.getWasteCount()).isEqualTo(0);
        assertThat(firstItem.getMyReaction()).isEqualTo("WISE");

        // N+1 문제 해결 검증: 각 메서드가 한 번씩만 호출되었는지 확인
        verify(consumptionRecordRepository, times(1)).findPublicFeedList(any(), any(), anyInt());
        verify(reactionRepository, times(1)).findReactionStatsByConsumptionRecordIds(anyList());
        verify(reactionRepository, times(1)).findByUserIdAndConsumptionRecordIds(any(), anyList());
    }

    @Test
    @DisplayName("빈 피드 리스트 조회")
    void getPublicFeedList_EmptyResult() {
        // given
        Long userId = 1L;
        Long cursorId = null;
        LocalDateTime cursorCreatedAt = null;
        int pageSize = 20;

        given(consumptionRecordRepository.findPublicFeedList(cursorId, cursorCreatedAt, pageSize))
                .willReturn(Collections.emptyList());

        // when
        FeedResponse.OptimizedFeedListResultDTO result = feedService.getPublicFeedList(userId, cursorId, cursorCreatedAt, pageSize);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFeedList()).isEmpty();
        assertThat(result.getFeedListSize()).isEqualTo(0);
        assertThat(result.getIsFirst()).isTrue();
        assertThat(result.getIsLast()).isTrue();
        assertThat(result.getHasNext()).isFalse();

        // 리액션 관련 메서드는 호출되지 않아야 함
        verify(reactionRepository, never()).findReactionStatsByConsumptionRecordIds(anyList());
        verify(reactionRepository, never()).findByUserIdAndConsumptionRecordIds(any(), anyList());
        
        // 기본 조회는 호출되어야 함
        verify(consumptionRecordRepository, times(1)).findPublicFeedList(cursorId, cursorCreatedAt, pageSize);
    }
}