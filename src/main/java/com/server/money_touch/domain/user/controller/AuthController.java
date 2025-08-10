package com.server.money_touch.domain.user.controller;

import com.server.money_touch.domain.user.converter.UserConverter;
import com.server.money_touch.domain.user.dto.UserResponse;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.service.user.AuthService;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import com.server.money_touch.global.validation.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "소셜 로그인", description = "유저에 관한 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class AuthController {

    private final AuthService authService;

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
    @GetMapping("/auth/login/kakao")
    public ApiResponse<UserResponse.UserCreateResultDTO> kakaoLogin(@RequestParam("code") String accessCode, @RequestParam("redirectUri") String redirectUri,HttpServletResponse httpServletResponse) {
        User user = authService.oAuthLogin(accessCode,redirectUri, httpServletResponse);
        return ApiResponse.onSuccess(UserConverter.toUserCreateResultDTO(user));
    }


}
