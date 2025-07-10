package com.server.money_touch.domain.consumptionRecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class FeedResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "피드 리스트 (피드 홈)")
    public static class FeedListResultDTO {

        @Schema(description = "게시글 목록")
        List<FeedDetailResultDTO> feedList;

        @Schema(description = "현재 페이지의 알림 개수", example = "10")
        Integer FeedListSize;

        @Schema(description = "페이지 처음 여부", example = "true")
        Boolean isFirst;

        @Schema(description = "페이지 마지막 여부", example = "false")
        Boolean isLast;

        @Schema(description = "다음 페이지가 있는지 여부", example = "true")
        Boolean hasNext;

    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "피드 상세 정보")
    public static class FeedDetailResultDTO {

        @Schema(description = "소비 기록 ID", example = "1")
        private Long consumptionRecordId;

        @Schema(description = "사용자 정보")
        private UserInfo user;

        @Schema(description = "카테고리 정보")
        private CategoryInfo consumptionCategory;

        @Schema(description = "소비 금액", example = "12000")
        private int amount;

        @Schema(description = "소비 내용", example = "신라방 마라탕")
        private String content;

        @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
        private String imageUrl;

        @Schema(description = "메모", example = "친구랑 같이 먹었어요!")
        private String memo;

        @Schema(description = "생성일시", example = "2024-03-15T14:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "현명해요 수", example = "5")
        private Integer wiseCount;

        @Schema(description = "낭비에요 수", example = "2")
        private Integer wasteCount;

        @Schema(description = "댓글 수", example = "3")
        private Integer commentCount;

        @Schema(description = "조회 수", example = "21")
        private Integer viewCount;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자 정보")
    public static class UserInfo {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "사용자 닉네임", example = "유저1")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImgUrl;

    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "카테고리 정보")
    public static class CategoryInfo {

        @Schema(description = "카테고리 ID", example = "1")
        private Long categoryId;

        @Schema(description = "카테고리 이름", example = "식비")
        private String budgetCategoryName;

    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "댓글 등록 응답 정보")
    public static class CommentResultDTO {

        @Schema(description = "댓글 ID", example = "10")
        private Long commentId;

    }

}
