package com.server.money_touch.domain.consumptionRecord.service.feed;

import com.server.money_touch.domain.consumptionRecord.converter.feed.FeedConverter;
import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.entity.Reaction;
import com.server.money_touch.domain.consumptionRecord.enums.FeedSortType;
import com.server.money_touch.domain.consumptionRecord.enums.MyFeedViewType;
import com.server.money_touch.domain.consumptionRecord.enums.ReactionType;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.consumptionRecord.repository.feed.FeedRepository;
import com.server.money_touch.domain.consumptionRecord.repository.reaction.ReactionRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final ConsumptionRecordRepository consumptionRecordRepository;
    private static final int PAGE_SIZE = 5;

    /**
     * 피드 상세 조회
     * @param userId 로그인한 사용자 ID
     * @param consumptionRecordId 조회할 소비기록 ID
     * @return FeedDetailResultDTO
     */
    @Override
    public FeedResponse.FeedDetailResultDTO getFeedDetail(Long userId, Long consumptionRecordId) {

        // 1. 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 소비기록 + 유저 + 카테고리 + 이미지 한번에 fetch join
        ConsumptionRecord record = feedRepository.findWithAllById(consumptionRecordId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND));

        // 3. 비공개 소비기록는 접근할 수 없음
        if (!record.isPublic()) {
            throw new ErrorHandler(ErrorStatus.FORBIDDEN_ACCESS_ON_PRIVATE_FEED);
        }

        // 4. 내가 남긴 리액션 조회 (null 가능)
        ReactionType myReactionType = reactionRepository.findByUserAndConsumptionRecord(user, record)
                .map(Reaction::getType)
                .orElse(null);


        // 5. 응답 변환
        return FeedConverter.toFeedDetailDTO(record, myReactionType);
    }

    @Transactional
    @Override
    public FeedResponse.ViewCountResultDTO increaseFeedViewCount(Long userId, Long consumptionRecordId) {

        // 1. 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 소비기록 조회
        ConsumptionRecord consumptionRecord = consumptionRecordRepository.findById(consumptionRecordId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND));

        // 3. 비공개 소비기록는 접근할 수 없음
        if (!consumptionRecord.isPublic()) {
            throw new ErrorHandler(ErrorStatus.FORBIDDEN_ACCESS_ON_PRIVATE_FEED);
        }

        // 4. 조회수 증가
        feedRepository.incrementViewCountIfPublic(consumptionRecordId);

        // 5. 갱신된 값 조회
        ConsumptionRecord updatedRecord = consumptionRecordRepository.findById(consumptionRecordId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND));

        // 6. DTO 반환
        return FeedConverter.toViewCountDTO(updatedRecord);
    }

    /**
     * 커서 기반 무한스크롤 피드 목록 조회
     * @param userId 로그인 사용자 ID
     * @param sortType 정렬 기준 ("view" or "latest")
     * @param cursorId 커서 ID (조회 기준 ID)
     * @param cursorViewCount 커서 조회수 (조회수순일 때만 사용)
     * @return FeedListResultDTO
     */
    @Override
    public FeedResponse.FeedListResultDTO getFeedsByCursor(Long userId, FeedSortType sortType, Long cursorId, Integer cursorViewCount) {

        // 1. 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        Slice<ConsumptionRecord> recordSlice;

        // 2. 정렬 방식에 따라
        if (sortType == FeedSortType.POPULAR) {
            recordSlice = feedRepository.findByCursorOrderByViewCountDesc(cursorViewCount, cursorId, pageable);
        } else {
            recordSlice = feedRepository.findByCursorOrderByIdDesc(cursorId, pageable);
        }

        // 소비기록 ID 리스트 추출
        List<Long> recordIds = recordSlice.getContent().stream()
                .map(ConsumptionRecord::getId)
                .toList();

        // 리액션 조회 (N+1 방지)
        Map<Long, ReactionType> myReactions = reactionRepository
                .findByUserAndPublicConsumptionRecordIds(user, recordIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getConsumptionRecord().getId(),
                        Reaction::getType
                ));

        return FeedConverter.toFeedListDTO(recordSlice, myReactions);
    }

    @Override
    public FeedResponse.FeedListResultDTO searchFeedsByUserNickname(String keyword, Long cursorId, Long userId) {

        // 1. 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, PAGE_SIZE);

        // 2. 피드 검색 (닉네임 포함 검색)
        Slice<ConsumptionRecord> recordSlice = feedRepository.searchFeedsByUserNickname(
                keyword,
                cursorId,
                pageable
        );

        // 3. 리액션 정보 조회
        List<Long> recordIds = recordSlice.getContent().stream()
                .map(ConsumptionRecord::getId)
                .toList();

        Map<Long, ReactionType> myReactions = reactionRepository
                .findByUserAndPublicConsumptionRecordIds(user, recordIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getConsumptionRecord().getId(),
                        Reaction::getType
                ));

        // 4. 변환 및 응답
        return FeedConverter.toFeedListDTO(recordSlice, myReactions);
    }

    @Override
    public FeedResponse.MyFeedListResultDTO getMyFeedsByCursor(
            Long userId,
            MyFeedViewType viewMode,
            Long cursorId
    ) {
        // 1. 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. viewMode에 따라 pageSize 다르게 설정
        int pageSize = switch (viewMode) {
            case CARD -> 20;
            case LIST -> 5;
        };

        Pageable pageable = PageRequest.of(0, pageSize);

        // 3. 피드 조회
        Slice<ConsumptionRecord> slice = feedRepository.findMyFeedsByCursorOrderByIdDesc(
                user.getId(),
                cursorId,
                pageable
                );

        // 4. 변환
        return FeedConverter.toMyFeedListDTO(slice);
    }

}

