package com.server.money_touch.domain.consumptionMbti.controller;


import com.server.money_touch.domain.consumptionMbti.dto.ConsumptionMbtiResponse;
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
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "소비 Mbti  페이지", description = "소비 Mbti 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/consumptionMbti")
public class ConsumptionMbtiController {

    // 소비 Mbti 조회
    @Operation(
            summary = "소비 Mbti 조회 API",
            description = "결과 코드(예: PTG)에 해당하는 소비 MBTI 설명, 부제, 이미지 등을 조회합니다."
    )
    @ApiSuccessCodeExample(resultClass = ConsumptionMbtiResponse.ConsumptionMbtiResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "MBTI_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "result", description = "조회하려는 소비 Mbti", example = "PTG", required = true),
    })
    @GetMapping("/result")
    public ApiResponse<ConsumptionMbtiResponse.ConsumptionMbtiResultDTO> getMbti(@RequestParam @NotBlank(message = "결과값은 필수입니다.") String result) {
        ConsumptionMbtiResponse.ConsumptionMbtiResultDTO response = null; //  TODO: 서비스 연결 예정
        return ApiResponse.onSuccess(response);
    }

}
