package com.server.money_touch.domain.consumptionRecord.controller;

import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionCategoryResponse;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordRequest;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordResponse;
import com.server.money_touch.domain.consumptionRecord.service.ConsumptionRecordService;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import com.server.money_touch.global.validation.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "소비 기록 페이지", description = "소비 기록에 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/consumptionrecord")
public class ConsumptionRecordController {

    private final ConsumptionRecordService consumptionRecordService;

    // 소비 기록 등록
    @Operation(
            summary = "소비 기록 등록 API",
            description = "소비 기록 등록 API 입니다."
    )
    @ApiSuccessCodeExample(resultClass = ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_CATEGORY_NOT_FOUND"),
    })
    @PostMapping("/record")
    public ApiResponse<ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO> postConsumptionRecord(
            @Valid @RequestBody ConsumptionRecordRequest.ConsumptionRecordCreateDTO request){

        // 유저 아이디 임시 지정
        ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO response = consumptionRecordService.createConsumptionRecord(1L, request);

        return ApiResponse.onSuccess(response);
    }

    // 소비 카테고리 목록 조회 API
    @Operation(summary = "소비 카테고리 목록 조회", description = "기본 + 커스텀 + 루틴 카테고리를 순서대로 반환합니다.")
    @ApiSuccessCodeExample(resultClass = ConsumptionCategoryResponse.CategoryInfoDTO.class)
    @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND")
    @GetMapping("/categories")
    public ApiResponse<List<ConsumptionCategoryResponse.CategoryInfoDTO>> getConsumptionCategories() {

        // 유저 아이디 임시 지정
        List<ConsumptionCategoryResponse.CategoryInfoDTO> categoryList =
                consumptionRecordService.getSortedCategoriesForUser(1L);
        return ApiResponse.onSuccess(categoryList);
    }

}
