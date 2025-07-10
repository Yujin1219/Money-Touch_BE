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

import java.util.List;

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

    @Operation(
            summary = "전체 소비 루틴 리스트 조회",
            description = "최신순으로 전체 소비 루틴을 조회합니다. 임시 더미데이터 입력한 상태입니다. "
                    + "Try it out -> Execute 로 리스트 확인 가능합니다."
                    + "당일 등록은 NEW 표시를 위해 true로 전달합니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_NOT_FOUND"),
    })
    @GetMapping("/list")
    public ApiResponse<List<RoutineResponse.RoutineListDTO>> getAllRoutines(){

        // TODO: 실제 데이터로 교체 예정. 임시 더미데이터
        List<RoutineResponse.RoutineListDTO> routines = List.of(
                new RoutineResponse.RoutineListDTO(
                        1L, true,"2025-07-09","50만원으로 한 달 살기 루틴",
                        "라인", "https://","https://",
                        List.of("#식비절약", "#생활비")
                ),

                new RoutineResponse.RoutineListDTO(
                        2L,false,"2025-06-10","커피값을 아끼자",
                        "오리", "https://","https://",
                        List.of("#카페지출줄이기","#커피절약")
                )
        );
        return ApiResponse.onSuccess(routines);
    }

}
