package com.server.money_touch.domain.user.controller;
import com.server.money_touch.domain.user.dto.UserRequest;
import com.server.money_touch.domain.user.dto.UserResponse;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import com.server.money_touch.global.validation.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "회원 가입 & 로그인 페이지", description = "유저에 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController{


    @Operation(
            summary = "유저 상세정보 저장 API",
            description = "필수 온보딩 과정에서 유저의 상세정보를 저장하는 API입니다.AccessToken 기반으로 유저를 식별합니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @PostMapping("/detail")
    public ApiResponse<UserResponse.UserDetailCreateResultDTO> createUserDetail(
            //@AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserRequest.UserDetailCreateDTO request){
            //Long userId = userPrincipal.getId(); // AccessToken에서 식별된 유저 ID
        UserResponse.UserDetailCreateResultDTO response = UserResponse.UserDetailCreateResultDTO.builder()
                .userId(1L)
                .build();
        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "로컬 회원가입 API",
            description = "이메일과 비밀번호를 사용한 로컬 회원가입 방식의 API입니다." + "이용약관 동의 리스트를 한꺼번에 보내주셔야합니다!"
    )
    @ApiSuccessCodeExample(resultClass = UserResponse.UserCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })

    @PostMapping("/signup")
    public ApiResponse<UserResponse.UserCreateResultDTO> signUpLocalUser(
            @Valid @RequestBody UserRequest.LocalSignUpDTO request){

        UserResponse.UserCreateResultDTO response = UserResponse.UserCreateResultDTO.builder()
                .userId(1L)
                .createdAt(LocalDateTime.now())
                .build();

        return ApiResponse.onSuccess(response);
    }
    @Operation(
            summary = "카카오 회원가입 API",
            description = "소셜 플랫폼의 액세스 토큰을 이용한 회원가입 API입니다." + "이용약관 동의 리스트 한꺼번에 보내주셔야합니다!"
    )
    @ApiSuccessCodeExample(resultClass = UserResponse.UserCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })

    @PostMapping("/kakao-signup")
    public ApiResponse<UserResponse.UserCreateResultDTO> signUpKakaoUser(
            @Valid @RequestBody UserRequest.KakaoSignUpDto request){

        UserResponse.UserCreateResultDTO response = UserResponse.UserCreateResultDTO.builder()
                .userId(1L)
                .createdAt(LocalDateTime.now())
                .build();

        return ApiResponse.onSuccess(response);
    }
    @Operation(
            summary = "로컬 로그인 API",
            description = "이메일과 비밀번호를 사용한 로컬 로그인 방식의 API입니다."
    )
    @ApiSuccessCodeExample(resultClass = UserResponse.LoginResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @PostMapping("/login")
    public ApiResponse<UserResponse.LoginResultDTO> localLogin(
            @Valid @RequestBody UserRequest.LocalLoginDTO request
    ){
        UserResponse.LoginResultDTO response = UserResponse.LoginResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }


    @Operation(
            summary = "소셜 로그인 API",
            description = "소셜 플랫폼의 액세스 토큰을 이용한 로그인 API입니다."
    )
    @ApiSuccessCodeExample(resultClass = UserResponse.LoginResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @PostMapping("/social-login")
    public ApiResponse<UserResponse.LoginResultDTO> socialLogin(
            @Valid @RequestBody UserRequest.SocialLoginRequest request
    ) {
        UserResponse.LoginResultDTO response = UserResponse.LoginResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }

    // 마이페이지
    @Operation(
            summary = "마이페이지 유저 정보 조회 API",
            description = "현재 로그인한 사용자의 마이페이지 정보를 조회하는 API입니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @GetMapping("/mypage")
    public ApiResponse<UserResponse.MyPageResponseDTO> getMyPage() {
        UserResponse.MyPageResponseDTO response = UserResponse.MyPageResponseDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }

    // 대표 배지 설정
    @Operation(
            summary = "대표 배지 설정 API",
            description = "획득한 배지 중에서 대표 배지를 설정하는 API입니다. UserBadge 테이블에서 획득 여부를 확인 후 User 엔티티의 badgeId를 업데이트합니다."
    )
//    @ApiSuccessCodeExample(resultClass = BadgeResponse.BadgeDetailResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BADGE_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BADGE_NOT_EARNED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @Parameters({
            @Parameter(name = "badgeId", description = "설정할 대표 배지 ID", example = "1", required = true)
    })
    @PatchMapping("/representative-badge/{badgeId}")
    public ApiResponse<UserResponse.RepresentativeBadgeResultDTO> setRepresentativeBadge(
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long badgeId
    ) {
        UserResponse.RepresentativeBadgeResultDTO response = UserResponse.RepresentativeBadgeResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }
}
