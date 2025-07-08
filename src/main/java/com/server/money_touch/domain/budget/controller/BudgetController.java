package com.server.money_touch.domain.budget.controller;

import com.server.money_touch.domain.budget.dto.BudgetRequest;
import com.server.money_touch.domain.budget.dto.BudgetResponse;
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

@Tag(name = "가계부 예산 페이지", description = "가계부 예산에 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    // 가계부 한 달 예산 등록
    @Operation(
            summary = "한 달 예산 등록 API",
            description = "총 예산과 카테고리별(기본, 사용자 정의, 소비 루틴) 예산 목록을 RequestBody로 입력받아 한 달 예산을 등록합니다."
    )
    @ApiSuccessCodeExample(resultClass = BudgetResponse.BudgetCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_EXCEEDED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_TOO_LOW"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @PostMapping()
    public ApiResponse<BudgetResponse.BudgetCreateResultDTO> postBudget(@Valid @RequestBody BudgetRequest.BudgetCreateDTO request) {
        BudgetResponse.BudgetCreateResultDTO response = BudgetResponse.BudgetCreateResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }


    // 한 달 예산 수정
    @Operation(
            summary = "한 달 예산 수정 API",
            description = "아이디와 일치하는 한 달 예산 수정 API 입니다. " +
                    "총 예산과 카테고리별(기본, 사용자 정의, 소비 루틴) 예산 목록을 RequestBody로 입력받아 한 달 예산을 수정합니다."
    )
    @ApiSuccessCodeExample(resultClass = BudgetResponse.BudgetCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BUDGET_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_EXCEEDED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_TOO_LOW"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "budgetId", description = "수정하려는 예산 아이디", example = "1", required = true),
    })
    @PatchMapping("/{budgetId}")
    public ApiResponse<BudgetResponse.BudgetCreateResultDTO> patchBudget(@Valid @RequestBody BudgetRequest.BudgetCreateDTO request,
                                                                         @RequestParam Long budgetId) {
        BudgetResponse.BudgetCreateResultDTO response = BudgetResponse.BudgetCreateResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }


    // 한 달 예산 내역 조회
    @Operation(
            summary = "한 달 예산 내역 조회 API",
            description = "아이디와 일치하는 한 달 예산 내역(에산 수정, 소비 루틴 등록 시) 조회 API 입니다."
    )
    @ApiSuccessCodeExample(resultClass = BudgetResponse.BudgetDetailDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BUDGET_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "budgetId", description = "조회하려는 예산 아이디", example = "1", required = true),
    })
    @GetMapping("/{budgetId}")
    public ApiResponse<BudgetResponse.BudgetDetailDTO> getBudget(@RequestParam Long budgetId) {
        BudgetResponse.BudgetDetailDTO response = BudgetResponse.BudgetDetailDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }


    // 한 달 예산 대비 총 소비 사용 금액 조회
    @Operation(
            summary = "가계부 한 달 총 소비 금액 조회 API",
            description = "한 달 예산 기준으로 현재까지의 총 소비 금액과 예산 소진 비율을 반환합니다."
    )
    @ApiSuccessCodeExample(resultClass = BudgetResponse.TotalConsumptionResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @GetMapping("/total-consumption")
    public ApiResponse<BudgetResponse.TotalConsumptionResultDTO> getTotalConsumption() {
        BudgetResponse.TotalConsumptionResultDTO response = BudgetResponse.TotalConsumptionResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }
}
