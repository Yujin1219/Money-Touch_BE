package com.server.money_touch.domain.routine.controller;

import com.server.money_touch.domain.routine.dto.RoutineRequest;
import com.server.money_touch.domain.routine.dto.RoutineResponse;
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

@Tag(name = "가계부 소비 루틴 페이지", description = "가계부 소비 루틴에 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/house-holds/routines")
public class RoutineController {

    // 소비 루틴 등록
    @Operation(
            summary = "소비 루틴 등록 API",
            description = "소비 루틴을 등록하는 API입니다. 예산 아이디는 Path Variable로 전달하며, 카테고리, 금액, 설명 등의 소비 루틴 정보는 RequestBody에 포함해 주세요."
    )
    @ApiSuccessCodeExample(resultClass = RoutineResponse.RoutineCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_ALREADY_EXIST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BUDGET_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_EXCEEDED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_TOO_LOW"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "budgetId", description = "한 달 예산 아이디", example = "1", required = true),
    })
    @PostMapping("/{budgetId}")
    public ApiResponse<RoutineResponse.RoutineCreateResultDTO> postRoutine(@Valid @RequestBody RoutineRequest.RoutineCreateDTO request,
                                                                    @PathVariable Long budgetId) {
        RoutineResponse.RoutineCreateResultDTO response = RoutineResponse.RoutineCreateResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }

    // 내 소비 루틴 목록 조회
    @Operation(
            summary = "내 소비 루틴 목록 조회 API",
            description = "가계부에서 사용자가 등록한 소비 루틴 목록을 조회하는 API입니다."
    )
//    @ApiSuccessCodeExample(resultClass = RoutineResponse.MyRoutineListDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @GetMapping("/users")
    public ApiResponse<RoutineResponse.MyRoutineListDTO> getMyRoutines() {
        RoutineResponse.MyRoutineListDTO response = RoutineResponse.MyRoutineListDTO.builder().build();

        return ApiResponse.onSuccess(response);
    }

    // 소비 루틴 이미지 등록?
}
