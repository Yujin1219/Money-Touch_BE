package com.server.money_touch.domain.consumptionRecord.controller;

import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "피드 페이지", description = "피드 조회 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/feed")
public class FeedController {

    // 피드 홈 (피드 리스트) 조회
    @Operation(
            summary = "피드 홈(피드 리스트) API",
            description = "공개된 소비 기록 피드를 조회하는 API입니다"
    )
//        @ApiSuccessCodeExample(resultClass = NotificationResponse.NotificationListDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @GetMapping("/home")
    public ApiResponse<FeedResponse.FeedListResultDTO> getFeedList() {
        FeedResponse.FeedListResultDTO response = FeedResponse.FeedListResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }

    // 피드 상세 조회
    @Operation(
            summary = "피드 상세 조회 API",
            description = "해당 피드를 눌렀을 때 피드의 상세 정보를 조회하는 API입니다"
    )
//        @ApiSuccessCodeExample(resultClass = NotificationResponse.NotificationListDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_RECORD_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "consumptionRecordId", description = "소비 기록 ID", example = "1", required = true)
    })
    @GetMapping("/{consumptionRecordId}")
    public ApiResponse<FeedResponse.FeedDetailResultDTO> getFeedDetail(@PathVariable Long consumptionRecordId) {
        FeedResponse.FeedDetailResultDTO response = FeedResponse.FeedDetailResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }
}
