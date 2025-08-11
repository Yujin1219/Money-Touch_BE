package com.server.money_touch.domain.user.controller;

import com.server.money_touch.domain.badge.service.BadgeCommandService;
import com.server.money_touch.domain.user.dto.UserRequest;
import com.server.money_touch.domain.user.dto.UserResponse;
import com.server.money_touch.domain.user.service.user.UserCommandService;
import com.server.money_touch.domain.user.service.user.UserQueryService;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.utils.AuthUtil;
import com.server.money_touch.global.utils.SendGridUtil;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import com.server.money_touch.global.validation.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

@Tag(name = "회원 가입 & 로그인 페이지", description = "유저에 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController{

    private final BadgeCommandService badgeCommandService;
    private final SendGridUtil sendGridUtil;
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final AuthUtil authUtil;

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
            @Valid @RequestBody UserRequest.UserDetailCreateDTO request, HttpServletRequest servletRequest){

        Long userId = authUtil.getUserIdFromRequest(servletRequest);
        UserResponse.UserDetailCreateResultDTO response = userCommandService.saveUserDetails(userId, request);
        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "이메일 인증 요청 API",
            description = "sendgrid를 이용하여 이메일로 인증번호를 발송합니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @GetMapping("/email/send")
    public ApiResponse<String> requestEmail(@RequestParam("to") String toEmail) {
        try {
            userQueryService.requestEmailVerification(toEmail);
            return ApiResponse.onSuccess("인증 이메일이 발송되었습니다.");
        } catch (IOException e) {
            // 로그 기록 추가 가능
            return ApiResponse.onFailure("SENDGRID4002","이메일 발송중 오류가 발생했습니다.", null);
        }
    }
    @Operation(summary = "이메일 인증 확인 API"
    ,description = "이메일 인증을 요청한 인증번호를 검증하는 API입니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @GetMapping("/email/verify")
    public ApiResponse<String> verifyAuthCode(@RequestParam String email, @RequestParam String code) {
        boolean isValid = sendGridUtil.verifyAuthCode(email, code);
        if (isValid) {
            return ApiResponse.onSuccess("인증이 완료되었습니다.");
        } else {
            return ApiResponse.onFailure("INVALID_CODE", "인증번호가 올바르지 않거나 만료되었습니다.", null);
        }
    }

    @Operation(
            summary = "닉네임 중복 인증 API",
            description = "닉네임 중복 인증을 하는 API입니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @GetMapping("/nickname")
    public ApiResponse<String> validateNickname(@RequestParam("nickname") String nickname) {
        try {
            boolean exists = userQueryService.existsByNickname(nickname);
            if (exists) {
                return ApiResponse.onFailure("USER4002", "이미 사용 중인 닉네임입니다.", null);
            }
            return ApiResponse.onSuccess("사용 가능한 닉네임입니다.");
        } catch (Exception e) {
            return ApiResponse.onFailure("SERVER500", "서버 오류가 발생했습니다.", null);
        }
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

    @PostMapping("/signup/local")
    public ApiResponse<UserResponse.UserCreateResultDTO> signUpLocalUser(
            @Valid @RequestBody UserRequest.LocalSignUpDTO request){

        UserResponse.UserCreateResultDTO response = userCommandService.signUpLocal(request);
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
    @PostMapping("/login/local")
    public ApiResponse<UserResponse.LoginResultDTO> loginLocalUser(
            @Valid @RequestBody UserRequest.LocalLoginDTO request) {

        UserResponse.LoginResultDTO response = userCommandService.loginLocal(request);

        return ApiResponse.onSuccess(response);
    }

    // 마이페이지
    @Operation(
            summary = "마이페이지 유저 정보 조회 API",
            description = "현재 로그인한 사용자의 마이페이지 정보를 조회하는 API입니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BADGE_NOT_EARNED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @GetMapping("/mypage")
    public ApiResponse<UserResponse.MyPageResponseDTO> getMyPage(HttpServletRequest servletrequest) {

        Long userId = authUtil.getUserIdFromRequest(servletrequest);
        UserResponse.MyPageResponseDTO response = userQueryService.getMyPageInfo(userId);
        return ApiResponse.onSuccess(response);
    }

}
