package com.server.money_touch.domain.user.dto;

import com.server.money_touch.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원 등록 응답 정보")
    public static class UserCreateResultDTO {

        @Schema(description = "유저 id", example = "1")
        private Long userId;

        private String accessToken;
        private String refreshToken;

        @Schema(description = "회원 생성일", example = "2021-11-08T11:44:30.327959"  )
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "유저 상세 정보 등록 응답 정보")
    public static class UserDetailCreateResultDTO{

        @Schema(description = "유저 상세 정보 ID", example = "1")
        private Long userDetailId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "로그인 응답 정보")
    public static class LoginResultDTO {
        private String accessToken;
        private String refreshToken;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "카카오 로그인 응답 정보")
    public static class OAuthLoginResultDTO {
        @Schema(description = "유저 id", example = "1")
        private Long userId;
        private String accessToken;
        private String refreshToken;
        private boolean isNewUser;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "마이페이지 프로필 정보")
    public static class MyPageResponseDTO {

        @Schema(description = "닉네임", example = "라인")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImgUrl;

        @Schema(description = "대표 배지 이미지 URL(없으면 null)", example = "https://example.com/badge.png")
        private String representativeBadgeImageUrl;
    }
}
