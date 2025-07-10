package com.server.money_touch.domain.consumptionRecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class FeedRequest {

    // 댓글 등록 요청 DTO
    @Getter
    @Schema(description = "댓글 등록 요청 정보")
    public static class CommentCreateDTO {

        @Schema(description = "소비 기록 ID", example = "1")
        @NotNull(message = "소비 기록 ID는 필수입니다.")
        private Long consumptionRecordId;

        @Schema(description = "부모 댓글 ID (일반 댓글이면 null)", example = "null")
        private Long parentId;

        @Schema(description = "댓글 내용 (1~300자)", example = "마라탕은 역시 신라방!")
        @NotNull(message = "댓글 내용은 필수입니다.")
        @Size(min = 1, max = 300, message = "댓글은 1자 이상 300자 이하로 작성해 주세요.")
        private String content;
    }

}
