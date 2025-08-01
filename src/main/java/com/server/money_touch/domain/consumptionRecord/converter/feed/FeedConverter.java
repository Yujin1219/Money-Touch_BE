package com.server.money_touch.domain.consumptionRecord.converter.feed;

import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecordImage;
import com.server.money_touch.domain.consumptionRecord.enums.ReactionType;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FeedConverter {

    /**
     * 소비기록 → 피드 상세 DTO 변환
     * @param record 소비기록 엔티티
     * @param myReaction 현재 로그인한 사용자의 반응 타입 (WISE/WASTE/null)
     * @return FeedDetailResultDTO
     */
    public static FeedResponse.FeedDetailResultDTO toFeedDetailDTO(ConsumptionRecord record, ReactionType myReaction) {
        return FeedResponse.FeedDetailResultDTO.builder()
                .consumptionRecordId(record.getId())
                .user(toUserInfo(record.getUser()))
                .consumptionCategory(toCategoryInfo(record.getConsumptionCategory()))
                .amount(record.getAmount())
                .content(record.getContent())
                .imageUrls(record.getImages().stream()
                        .map(ConsumptionRecordImage::getFilePath)
                        .collect(Collectors.toList()))
                .memo(record.getMemo())
                .createdAt(record.getCreatedAt())
                .wiseCount(record.getWiseCount())
                .wasteCount(record.getWasteCount())
                .commentCount(record.getCommentCount())
                .viewCount(record.getViewCount())
                .myReaction(myReaction)
                .build();
    }

    /**
     * 피드리스트 전용 게시글 하나
     */
    public static FeedResponse.FeedListItemDTO toFeedListItemDTO(ConsumptionRecord record, ReactionType myReaction) {

        return FeedResponse.FeedListItemDTO.builder()
                .consumptionRecordId(record.getId())
                .user(toUserInfo(record.getUser()))
                .imageUrls(record.getImages().stream()
                        .map(ConsumptionRecordImage::getFilePath)
                        .collect(Collectors.toList()))
                .createdAt(record.getCreatedAt())
                .wiseCount(record.getWiseCount())
                .wasteCount(record.getWasteCount())
                .viewCount(record.getViewCount())
                .myReaction(myReaction)
                .build();
    }

    /**
     * 커서 기반 무한스크롤 Slice<ConsumptionRecord> → FeedListResultDTO 변환
     */
    public static FeedResponse.FeedListResultDTO toFeedListDTO(
            Slice<ConsumptionRecord> slice,
            Map<Long, ReactionType> myReactions // 각 게시물에 대한 내 리액션 정보
    ) {
        if (slice == null || slice.isEmpty()) {
            return FeedResponse.FeedListResultDTO.builder()
                    .feedList(List.of())
                    .FeedListSize(0)
                    .isFirst(true)
                    .hasNext(false)
                    .nextCursorId(null)
                    .nextCursorViewCount(null)
                    .build();
        }

        List<FeedResponse.FeedListItemDTO> feedList = slice.getContent().stream()
                .map(record -> toFeedListItemDTO(record, myReactions.get(record.getId())))
                .toList();

        Long nextCursorId = null;
        Integer nextCursorViewCount = null;

        if (slice.hasNext() && !feedList.isEmpty()) {
            ConsumptionRecord lastRecord = slice.getContent().get(slice.getContent().size() - 1);
            nextCursorId = lastRecord.getId();
            nextCursorViewCount = lastRecord.getViewCount();
        }

        return FeedResponse.FeedListResultDTO.builder()
                .feedList(feedList)
                .FeedListSize(feedList.size())
                .isFirst(slice.isFirst())
                .hasNext(slice.hasNext())
                .nextCursorId(nextCursorId)
                .nextCursorViewCount(nextCursorViewCount)
                .build();
    }

    public static FeedResponse.MyFeedListResultDTO toMyFeedListDTO(Slice<ConsumptionRecord> slice) {
        if (slice == null || slice.isEmpty()) {
            return FeedResponse.MyFeedListResultDTO.builder()
                    .feedList(List.of())
                    .FeedListSize(0)
                    .isFirst(true)
                    .hasNext(false)
                    .nextCursorId(null)
                    .build();
        }

        // 각 ConsumptionRecord를 MyFeedItemDTO로 변환
        List<FeedResponse.MyFeedItemDTO> items = slice.getContent().stream()
                .map(record -> FeedResponse.MyFeedItemDTO.builder()
                        .consumptionRecordId(record.getId())
                        .userId(record.getUser().getId())
                        .imageUrls(record.getImages().stream()
                                .map(ConsumptionRecordImage::getFilePath)
                                .collect(Collectors.toList()))
                        .content(record.getContent())
                        .amount(record.getAmount())
                        .createdAt(record.getCreatedAt())
                        .build()
                ).toList();

        // 다음 커서 ID 설정
        Long nextCursorId = null;
        if (slice.hasNext() && !items.isEmpty()) {
            ConsumptionRecord lastRecord = slice.getContent().get(slice.getContent().size() - 1);
            nextCursorId = lastRecord.getId();
        }

        return FeedResponse.MyFeedListResultDTO.builder()
                .feedList(items)
                .FeedListSize(items.size())
                .isFirst(slice.isFirst())
                .hasNext(slice.hasNext())
                .nextCursorId(nextCursorId)
                .build();
    }


    // 사용자 정보 반환
    public static FeedResponse.UserInfo toUserInfo(User user) {
        return FeedResponse.UserInfo.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImgUrl())
                .build();
    }

    // 카티고리 정보 반환
    public static FeedResponse.CategoryInfo toCategoryInfo(ConsumptionCategory category) {
        return FeedResponse.CategoryInfo.builder()
                .categoryId(category.getId())
                .budgetCategoryName(category.getBudgetCategoryName())
                .build();
    }

    // 조회수 증가 결과 반환
    public static FeedResponse.ViewCountResultDTO toViewCountDTO(ConsumptionRecord record) {
        return FeedResponse.ViewCountResultDTO.builder()
                .consumptionRecordId(record.getId())
                .viewCount(record.getViewCount())
                .build();
    }
}
