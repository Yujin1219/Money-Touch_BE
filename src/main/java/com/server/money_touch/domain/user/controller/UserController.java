package com.server.money_touch.domain.user.controller;
import com.server.money_touch.domain.user.dto.UserRequest;
import com.server.money_touch.domain.user.dto.UserResponse;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import com.server.money_touch.global.validation.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "회원 가입 페이지", description = "회원가입에 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController{

    // 회원 가입
    @Operation(
            summary = "로컬 회원가입 API",
            description = "로컬 로그인 방식의 회원 가입 API 입니다."
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
            description = "카카오 소셜 로그인 방식의 회원 가입 API 입니다."
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
}
