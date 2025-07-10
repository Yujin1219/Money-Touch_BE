package com.server.money_touch.domain.consumptionRecord.controller;

import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordRequest;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordResponse;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "소비 기록 페이지", description = "소비 기록에 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/consumptionrecord")
public class ConsumptionRecordController {

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
    })

    @PostMapping("/record")
    public ApiResponse<ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO> postConsumptionRecord(
            @Valid @RequestBody ConsumptionRecordRequest.ConsumptionRecordCreateDTO request){

        ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO response = null;

        return ApiResponse.onSuccess(response);
    }

}
