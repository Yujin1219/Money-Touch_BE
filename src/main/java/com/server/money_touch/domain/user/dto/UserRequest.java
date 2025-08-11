package com.server.money_touch.domain.user.dto;

import com.server.money_touch.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class    UserRequest{

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "유저 상세정보 등록 요청 정보")
    public static class UserDetailCreateDTO{

        @Schema(description = "나이" , example = "20")
        @NotBlank(message = "나이값은 필수입니다.")
        private String age;

        @Schema(description = "성별 (남성 : MALE / 여성 : FEMALE", example = "MALE")
        private Gender gender;

        @Schema(description = "직업", example = "학생")
        @NotBlank(message = "직업값은 필수입니다.")
        private String job;

        @Schema(description = "수입 여부", example = "yes")
        @NotBlank(message = "수입 여부는 필수값입니다.")
        private String isIncome;

        @Schema(description = "사진 URL", example = "http://example.com/image.jpg")
        private String profileImgUrl;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "로컬 회원가입 정보")
    public static class LocalSignUpDTO{

        @Schema(description = "이메일", example = "example@naver.com")
        @Email(message = "올바른 이메일 형식이어야합니다")
        @NotBlank(message = "이메일은 필수입니다")
        private String email;

        @Schema(description = "비밀번호", example = "12345678Abc@")
        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;

        @Schema(description = "약관동의 리스트")
        private List<AgreementDTO> agreeTerms;

        @Schema(description = "닉네임", example = "잔디")
        @NotBlank
        private String nickname;

        @Schema(description = "사진 URL", example = "http://example.com/image.jpg")
        private String profileImgUrl;


    };

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "소셜 회원가입 정보")
    public static class KakaoSignUpDto {

        @Schema(description = "카카오 ID", example = "1234567890")
        @NotBlank(message = "카카오 사용자 ID는 필수입니다.")
        private String kakaoKey; // 카카오에서 받은 고유 식별자

        @Schema(description = "약관동의 리스트")
        private List<AgreementDTO> agreeTerms;

        @Schema(description = "닉네임", example = "잔디")
        private String nickname;

        // 선택: 카카오에서 프로필 사진을 제공한 경우
        @Schema(description = "사진 URL", example = "http://example.com/image.jpg")
        private String profileImgUrl;

        // 선택: 카카오에서 이메일을 제공한 경우
        @Schema(description = "이메일", example = "example@kakao.com")
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "로컬 로그인 요청 정보")
    public static class LocalLoginDTO {
        @Schema(description = "이메일", example = "example@naver.com")
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        private String email;

        @Schema(description = "비밀번호" , example = "12345678Abc@")
        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "소셜 로그인 요청 정보")
    public static class SocialLoginRequest {
        @Schema(description = "소셜에서 발급받은 토큰", example = "asdfjqrer")
        @NotBlank(message = "토큰값은 필수입니다.")
        private String accessToken;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "이용 약관 요청 정보")
    public static class AgreementDTO {

        @Schema(description = "약관 ID", example = "1")
        @NotNull
        private Long termsId;

        @Schema(description = "동의여부", example = "true")
        @NotNull
        private Boolean isAgree;
    }
}