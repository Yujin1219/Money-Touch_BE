package com.server.money_touch.domain.consumptionRecord.dto;

import com.server.money_touch.domain.consumptionRecord.enums.ReactionType;
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
        List<FeedListItemDTO> feedList;

        @Schema(description = "현재 페이지의 알림 개수", example = "10")
        Integer FeedListSize;

        @Schema(description = "페이지 처음 여부", example = "true")
        Boolean isFirst;

        @Schema(description = "다음 페이지가 있는지 여부", example = "true")
        Boolean hasNext;

        @Schema(description = "다음 커서 ID (무한스크롤용)", example = "1")
        private Long nextCursorId;

        @Schema(description = "다음 커서 조회수", example = "20")
        private Integer nextCursorViewCount;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "피드 리스트 아이템 (리스트 전용)")
    public static class FeedListItemDTO {

        @Schema(description = "소비기록 ID", example = "1")
        private Long consumptionRecordId;

        @Schema(description = "사용자 정보")
        private UserInfo user;

        @Schema(description = "이미지 URL 리스트", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
        private List<String> imageUrls;

        @Schema(description = "생성일시", example = "2024-03-15T14:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "현명해요 수", example = "5")
        private Integer wiseCount;

        @Schema(description = "낭비에요 수", example = "1")
        private Integer wasteCount;

        @Schema(description = "조회 수", example = "21")
        private Integer viewCount;

        @Schema(description = "현재 내가 누른 리액션 타입 (없으면 null)", example = "WISE")
        private ReactionType myReaction;
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

        @Schema(description = "이미지 URL 리스트", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
        private List<String> imageUrls;

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

        @Schema(description = "현재 내가 누른 리액션 타입 (없으면 null)", example = "WISE")
        private ReactionType myReaction;

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

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "댓글 + 대댓글 조회 응답 정보")
    public static class CommentListDTO {

        @Schema(description = "댓글 ID", example = "10")
        private Long commentId;

        @Schema(description = "댓글 작성자 ID", example = "1")
        private Long userId;

        @Schema(description = "댓글 작성자 닉네임", example = "유저1")
        private String nickname;

        @Schema(description = "댓글 작성자 프로필 이미지 url", example = "https://example.com/profile.jpg")
        private String profileImgUrl;

        @Schema(description = "댓글 내용", example = "마라탕 맛있죠!")
        private String content;

        @Schema(description = "좋아요 수", example = "3")
        private Integer likes;

        @Schema(description = "내가 좋아요를 눌렀는지 여부", example = "true")
        private boolean liked;

        @Schema(description = "작성 시간", example = "2024-07-28T13:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "대댓글 목록")
        private List<CommentListDTO> replies;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "댓글 좋아요 응답 정보")
    public static class CommentLikeResultDTO {

        @Schema(description = "댓글 ID", example = "1")
        private Long commentId;

        @Schema(description = "좋아요 개수", example = "8")
        private int likeCount;

        @Schema(description = "현재 사용자가 좋아요를 눌렀는지 여부", example = "true")
        private boolean liked;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "리액션 결과 DTO")
    public static class ReactionResultDTO {

        @Schema(description = "소비기록 ID", example = "1")
        private Long consumptionRecordId;

        @Schema(description = "변경된 현명해요 수", example = "5")
        private Integer wiseCount;

        @Schema(description = "변경된 낭비에요 수", example = "2")
        private Integer wasteCount;

        @Schema(description = "현재 내가 누른 리액션 타입 (없으면 null)", example = "WISE")
        private String myReaction;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "조회수 증가 결과 DTO")
    public static class ViewCountResultDTO {

        @Schema(description = "소비 기록 ID", example = "1")
        private Long consumptionRecordId;

        @Schema(description = "증가된 조회수", example = "22")
        private Integer viewCount;

    }
}
