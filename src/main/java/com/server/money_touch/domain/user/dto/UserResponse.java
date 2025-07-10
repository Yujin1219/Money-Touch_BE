package com.server.money_touch.domain.user.dto;

import com.server.money_touch.domain.badge.entity.Badge;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "마이페이지 프로필 정보")
    public static class MyPageResponseDTO {
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "닉네임", example = "라인")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImgUrl;

        @Schema(description = "대표 뱃지 ID", example = "1")
        private Badge badgeId;
    }
}
