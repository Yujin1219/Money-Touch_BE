package com.server.money_touch.domain.consumptionRecord.service.reaction;

import com.server.money_touch.domain.consumptionRecord.dto.FeedRequest;
import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.entity.Reaction;
import com.server.money_touch.domain.consumptionRecord.enums.ReactionType;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.consumptionRecord.repository.reaction.ReactionRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceImplTest {

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConsumptionRecordRepository consumptionRecordRepository;

    @InjectMocks
    private ReactionServiceImpl reactionService;

    private User user;
    private ConsumptionRecord consumptionRecord;
    private FeedRequest.ReactionCreateDTO reactionCreateDTO;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        consumptionRecord = mock(ConsumptionRecord.class);
        reactionCreateDTO = mock(FeedRequest.ReactionCreateDTO.class);
    }

    @Test
    @DisplayName("새로운 리액션 생성 성공")
    void addOrUpdateReaction_NewReaction_Success() {
        // given
        Long userId = 1L;
        Long consumptionRecordId = 1L;
        given(reactionCreateDTO.getType()).willReturn(ReactionType.WISE);
        
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(consumptionRecordRepository.findById(consumptionRecordId)).willReturn(Optional.of(consumptionRecord));
        given(reactionRepository.findByUserAndConsumptionRecord(user, consumptionRecord)).willReturn(Optional.empty());
        given(reactionRepository.countWiseReactionsByConsumptionRecord(consumptionRecord)).willReturn(1);
        given(reactionRepository.countWasteReactionsByConsumptionRecord(consumptionRecord)).willReturn(0);

        // when
        FeedResponse.ReactionResultDTO result = reactionService.addOrUpdateReaction(userId, consumptionRecordId, reactionCreateDTO);

        // then
        verify(reactionRepository).save(any(Reaction.class));
        assertThat(result.getConsumptionRecordId()).isEqualTo(consumptionRecordId);
        assertThat(result.getWiseCount()).isEqualTo(1);
        assertThat(result.getWasteCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("기존 리액션 취소 성공")
    void addOrUpdateReaction_CancelExistingReaction_Success() {
        // given
        Long userId = 1L;
        Long consumptionRecordId = 1L;
        Reaction existingReaction = mock(Reaction.class);
        given(reactionCreateDTO.getType()).willReturn(ReactionType.WISE);
        given(existingReaction.getType()).willReturn(ReactionType.WISE);
        
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(consumptionRecordRepository.findById(consumptionRecordId)).willReturn(Optional.of(consumptionRecord));
        given(reactionRepository.findByUserAndConsumptionRecord(user, consumptionRecord)).willReturn(Optional.of(existingReaction));
        given(reactionRepository.countWiseReactionsByConsumptionRecord(consumptionRecord)).willReturn(0);
        given(reactionRepository.countWasteReactionsByConsumptionRecord(consumptionRecord)).willReturn(0);

        // when
        FeedResponse.ReactionResultDTO result = reactionService.addOrUpdateReaction(userId, consumptionRecordId, reactionCreateDTO);

        // then
        verify(reactionRepository).delete(existingReaction);
        assertThat(result.getConsumptionRecordId()).isEqualTo(consumptionRecordId);
        assertThat(result.getWiseCount()).isEqualTo(0);
        assertThat(result.getWasteCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("기존 리액션 타입 변경 성공")
    void addOrUpdateReaction_UpdateReactionType_Success() {
        // given
        Long userId = 1L;
        Long consumptionRecordId = 1L;
        Reaction existingReaction = mock(Reaction.class);
        given(reactionCreateDTO.getType()).willReturn(ReactionType.WISE);
        given(existingReaction.getType()).willReturn(ReactionType.WASTE);
        
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(consumptionRecordRepository.findById(consumptionRecordId)).willReturn(Optional.of(consumptionRecord));
        given(reactionRepository.findByUserAndConsumptionRecord(user, consumptionRecord)).willReturn(Optional.of(existingReaction));
        given(reactionRepository.countWiseReactionsByConsumptionRecord(consumptionRecord)).willReturn(1);
        given(reactionRepository.countWasteReactionsByConsumptionRecord(consumptionRecord)).willReturn(0);

        // when
        FeedResponse.ReactionResultDTO result = reactionService.addOrUpdateReaction(userId, consumptionRecordId, reactionCreateDTO);

        // then
        verify(existingReaction).updateType(ReactionType.WISE);
        assertThat(result.getConsumptionRecordId()).isEqualTo(consumptionRecordId);
        assertThat(result.getWiseCount()).isEqualTo(1);
        assertThat(result.getWasteCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 - 예외 발생")
    void addOrUpdateReaction_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L;
        Long consumptionRecordId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reactionService.addOrUpdateReaction(userId, consumptionRecordId, reactionCreateDTO))
                .isInstanceOf(GeneralException.class)
                .hasFieldOrPropertyWithValue("code", ErrorStatus.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 소비 기록 - 예외 발생")
    void addOrUpdateReaction_ConsumptionRecordNotFound_ThrowsException() {
        // given
        Long userId = 1L;
        Long consumptionRecordId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(consumptionRecordRepository.findById(consumptionRecordId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reactionService.addOrUpdateReaction(userId, consumptionRecordId, reactionCreateDTO))
                .isInstanceOf(GeneralException.class)
                .hasFieldOrPropertyWithValue("code", ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND);
    }

    @Test
    @DisplayName("리액션 통계 조회 성공")
    void getReactionStats_Success() {
        // given
        Long userId = 1L;
        Long consumptionRecordId = 1L;
        Reaction userReaction = mock(Reaction.class);
        given(userReaction.getType()).willReturn(ReactionType.WISE);
        
        given(consumptionRecordRepository.findById(consumptionRecordId)).willReturn(Optional.of(consumptionRecord));
        given(reactionRepository.countWiseReactionsByConsumptionRecord(consumptionRecord)).willReturn(5);
        given(reactionRepository.countWasteReactionsByConsumptionRecord(consumptionRecord)).willReturn(2);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reactionRepository.findByUserAndConsumptionRecord(user, consumptionRecord)).willReturn(Optional.of(userReaction));

        // when
        FeedResponse.ReactionResultDTO result = reactionService.getReactionStats(consumptionRecordId, userId);

        // then
        assertThat(result.getConsumptionRecordId()).isEqualTo(consumptionRecordId);
        assertThat(result.getWiseCount()).isEqualTo(5);
        assertThat(result.getWasteCount()).isEqualTo(2);
        assertThat(result.getMyReaction()).isEqualTo("WISE");
    }
}