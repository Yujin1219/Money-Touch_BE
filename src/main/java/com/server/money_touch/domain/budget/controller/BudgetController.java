package com.server.money_touch.domain.budget.controller;

import com.server.money_touch.domain.budget.dto.BudgetRequest;
import com.server.money_touch.domain.budget.dto.BudgetResponse;
import com.server.money_touch.domain.budget.service.budget.BudgetCommandService;
import com.server.money_touch.domain.budget.service.budget.BudgetQueryService;
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
@RequestMapping("/api/house-holds/budgets")
public class BudgetController {

    private final BudgetCommandService budgetCommandService;
    private final BudgetQueryService budgetQueryService;

    // 가계부 한 달 예산 등록
    @Operation(
            summary = "월간 예산 등록 또는 수정 API",
            description = "총 예산과 기본, 사용자 정의, 소비 루틴 카테고리별 예산 목록을 RequestBody로 입력받아 해당 월의 예산을 등록하거나 수정합니다. " +
                    "이미 등록된 예산이 있는 경우에는 수정됩니다."
    )
    @ApiSuccessCodeExample(resultClass = BudgetResponse.BudgetCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_EXCEEDED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_TOO_LOW"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BUDGET_ALREADY_EXIST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_CATEGORY_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @PostMapping()
    public ApiResponse<BudgetResponse.BudgetCreateResultDTO> postBudget(@Valid @RequestBody BudgetRequest.BudgetCreateDTO request) {
        // 로그인 전까지 userId 1로 임시 세팅
        BudgetResponse.BudgetCreateResultDTO response = budgetCommandService.registerOrUpdateBudgetWithCategories(1L, request);
        return ApiResponse.onSuccess(response);
    }

    // 한 달 예산 내역 조회
    @Operation(
            summary = "한 달 예산 내역 조회 API",
            description = "아이디와 일치하는 한 달 예산 내역 조회 API 입니다. (소비 루틴 등록 시 나의 한 달 예산 정보를 불러오는 용도)"
    )
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
    public ApiResponse<BudgetResponse.BudgetDetailDTO> getBudget(@PathVariable Long budgetId) {
        // 로그인 전까지 userId 1로 임시 세팅
        BudgetResponse.BudgetDetailDTO response = budgetQueryService.findBudgetById(1L, budgetId);
        return ApiResponse.onSuccess(response);
    }

    // 한 달 예산 기준 총 소비 사용 금액 조회
    @Operation(
            summary = "예산 아이디 및 한 달 총 소비 금액 조회 API",
            description = "한 달 예산을 기준으로 예산 아이디 및 현재까지의 총 소비 금액을 반환합니다. " +
                    "등록된 예산이 없는 경우, 에러 메시지를 반환합니다."
    )
    @ApiSuccessCodeExample(resultClass = BudgetResponse.TotalConsumptionResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BUDGET_NOT_EXIST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "year", description = "조회하려는 연도", example = "2025", required = true),
            @Parameter(name = "month", description = "조회하려는 월", example = "7", required = true),
    })
    @GetMapping("/total-consumption")
    public ApiResponse<BudgetResponse.TotalConsumptionResultDTO> getTotalConsumption(@RequestParam Integer year, @RequestParam Integer month) {
        // 로그인 전까지 userId 1로 임시 세팅
        BudgetResponse.TotalConsumptionResultDTO response = budgetQueryService.findBudgetByIdAndTotalConsumption(1L, year, month);
        return ApiResponse.onSuccess(response);
    }
}
