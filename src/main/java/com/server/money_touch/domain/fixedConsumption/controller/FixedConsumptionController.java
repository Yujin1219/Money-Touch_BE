package com.server.money_touch.domain.fixedConsumption.controller;

import com.server.money_touch.domain.fixedConsumption.dto.FixedConsumptionRequest;
import com.server.money_touch.domain.fixedConsumption.dto.FixedConsumptionResponse;
import com.server.money_touch.domain.fixedConsumption.service.FixedConsumptionCommandService;
import com.server.money_touch.domain.fixedConsumption.service.FixedConsumptionQueryService;
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

@Tag(name = "가계부 고정비 페이지", description = "가계부 고정비에 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/house-holds/fixed-consumptions")
public class FixedConsumptionController {

    private final FixedConsumptionCommandService fixedConsumptionCommandService;
    private final FixedConsumptionQueryService fixedConsumptionQueryService;

    @Operation(
            summary = "고정비 등록 API",
            description = "고정비 등록 API 입니다. 금액, 카테고리, 항목명, 메모를 RequestBody로 입력받아 고정비를 등록합니다."
    )
    @ApiSuccessCodeExample(resultClass = FixedConsumptionResponse.FixedConsumptionCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_CATEGORY_NAME_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @PostMapping()
    public ApiResponse<FixedConsumptionResponse.FixedConsumptionCreateResultDTO> postFixedConsumption(@Valid @RequestBody FixedConsumptionRequest.FixedConsumptionCreateDTO request) {
        // 로그인 전까지 userId 1로 임시 세팅
        FixedConsumptionResponse.FixedConsumptionCreateResultDTO response = fixedConsumptionCommandService.saveFixedConsumption(1L, request);
        return ApiResponse.onSuccess(response);
    }


    @Operation(
            summary = "고정비 수정 API",
            description = "고정비 ID를 통해 등록된 항목을 찾아, 금액·카테고리·항목명·메모를 수정하는 API입니다. " +
                    "ID는 PathVariable로, 수정 정보는 RequestBody로 입력받습니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "FIXED_CONSUMPTION_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "fixedConsumptionId", description = "수정하려는 고정비 아이디", example = "1", required = true),
    })
    @PatchMapping("/{fixedConsumptionId}")
    public ApiResponse<String> patchFixedConsumption(@Valid @RequestBody FixedConsumptionRequest.FixedConsumptionCreateDTO request,
                                               @PathVariable Long fixedConsumptionId) {
        // 로그인 전까지 userId 1로 임시 세팅
        fixedConsumptionCommandService.updateFixedConsumption(1L, fixedConsumptionId, request);
        return ApiResponse.onSuccess("고정비 수정 성공");
    }


    @Operation(
            summary = "고정비 삭제 API",
            description = "고정비 ID를 통해 등록된 항목을 찾아, 고정비를 삭제하는 API 입니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "FIXED_CONSUMPTION_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "fixedConsumptionId", description = "삭제하려는 고정비 아이디", example = "1", required = true),
    })
    @DeleteMapping("/{fixedConsumptionId}")
    public ApiResponse<String> deleteFixedConsumption(@PathVariable Long fixedConsumptionId) {
        // 로그인 전까지 userId 1로 임시 세팅
        fixedConsumptionCommandService.deleteFixedConsumption(1L, fixedConsumptionId);
        return ApiResponse.onSuccess("고정비 삭제 성공");
    }

    @Operation(
            summary = "고정비 목록 조회 API",
            description = "사용자의 고정비 목록을 커서 기반 무한스크롤로 조회하는 API 입니다. 커서를 쿼리 파라미터로 입력해 주세요."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "cursorId", description = "커서 (이전 요청의 마지막 consumptionRecordId). 첫 요청 시 생략", example = "3", required = false)
    })
    @GetMapping("list")
    public ApiResponse<FixedConsumptionResponse.FixedConsumptionCursorResultDTO> getFixedConsumptions(@RequestParam(required = false) Long cursorId) {
        // 로그인 전까지 userId 1로 임시 세팅
        FixedConsumptionResponse.FixedConsumptionCursorResultDTO response = fixedConsumptionQueryService.getFixedConsumptions(1L, cursorId);
        return ApiResponse.onSuccess(response);
    }

}
