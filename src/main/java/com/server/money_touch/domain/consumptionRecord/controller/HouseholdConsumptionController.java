package com.server.money_touch.domain.consumptionRecord.controller;

import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordResponse;
import com.server.money_touch.domain.consumptionRecord.service.ConsumptionRecordCommandService;
import com.server.money_touch.domain.consumptionRecord.service.ConsumptionRecordQueryService;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import com.server.money_touch.global.validation.annotation.ApiSuccessCodeExample;
import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionRequest;
import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "가계부 소비 페이지", description = "가계부 소비에 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/house-holds/consumptions")
public class HouseholdConsumptionController {

    private final ConsumptionRecordCommandService consumptionRecordCommandService;
    private final ConsumptionRecordQueryService consumptionRecordQueryService;

    // 가계부 일일 소비 등록
    @Operation(
            summary = "일일 소비 등록 API",
            description = "가계부에서 일일 소비 내역을 등록하는 API 입니다."
    )
    @ApiSuccessCodeExample(resultClass = ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_CATEGORY_NAME_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @PostMapping("/daily")
    public ApiResponse<ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO> postDailyConsumptionRecord(
            @Valid @RequestBody HouseholdConsumptionRequest.DailyConsumptionCreateDTO request){

        // 로그인 전까지 userId 1로 임시 세팅
        ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO response = consumptionRecordCommandService.saveDailyConsumptionRecord(1L, request);
        return ApiResponse.onSuccess(response);
    }

    // 가계부 일일 소비 수정
    @Operation(
            summary = "일일 소비 수정 API",
            description = "가계부에서 아이디와 일치하는 일일 소비 내역을 수정하는 API 입니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_RECORD_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_CATEGORY_NAME_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "consumptionRecordId", description = "수정하려는 소비 기록 아이디", example = "1", required = true),
    })
    @PatchMapping("/daily/{consumptionRecordId}")
    public ApiResponse<String> patchDailyConsumptionRecord(@Valid @RequestBody HouseholdConsumptionRequest.DailyConsumptionCreateDTO request,
                                                      @PathVariable Long consumptionRecordId){
        // 로그인 전까지 userId 1로 임시 세팅
        consumptionRecordCommandService.updateDailyConsumptionRecord(1L, consumptionRecordId, request);

        return ApiResponse.onSuccess("일일 소비 수정 성공");
    }

    // 가계부 일일 소비 삭제
    @Operation(
            summary = "일일 소비 삭제 API",
            description = "가계부에서 아이디와 일치하는 일일 소비 내역을 삭제하는 API 입니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_RECORD_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "consumptionRecordId", description = "삭제하려는 소비 기록 아이디", example = "1", required = true),
    })
    @DeleteMapping("/daily/{consumptionRecordId}")
    public ApiResponse<String> deleteDailyConsumptionRecord(@PathVariable Long consumptionRecordId){
        // 로그인 전까지 userId 1로 임시 세팅
        consumptionRecordCommandService.deleteDailyConsumptionRecord(1L, consumptionRecordId);

        return ApiResponse.onSuccess("일일 소비 삭제 성공");
    }

    // 가계부 일일 소비 내역 조회
    @Operation(
            summary = "일일 소비 내역 조회 API",
            description = "가계부에서 아이디와 일치하는 일일 소비 내역을 조회하는 API 입니다. " +
                    "(일일 소비 내역 수정 시 정보 불러오는 용도)"
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_RECORD_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "consumptionRecordId", description = "조회하려는 소비 기록 아이디", example = "1", required = true),
    })
    @GetMapping("/daily/{consumptionRecordId}")
    public ApiResponse<HouseholdConsumptionResponse.DailyConsumptionDetailDTO> getDailyConsumptionRecord(@PathVariable Long consumptionRecordId){

        // 로그인 전까지 userId 1로 임시 세팅
        HouseholdConsumptionResponse.DailyConsumptionDetailDTO response = consumptionRecordQueryService.getDailyConsumptionRecordDetail(1L, consumptionRecordId);
        return ApiResponse.onSuccess(response);
    }

    // 가계부 해당 월의 소비 내역 목록 조회
    @Operation(
            summary = "해당 월의 일일 소비 내역 목록 조회 API",
            description = "가계부에서 특정 월의 일일 소비 내역 목록을 스크롤 형식으로 조회하는 API입니다. 연도(year)와 월(month)을 쿼리 파라미터로 입력하세요."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "year", description = "조회하려는 소비 연도", example = "2025", required = true),
            @Parameter(name = "month", description = "조회하려는 소비 월", example = "7", required = true),
            @Parameter(name = "cursorId", description = "커서 (이전 요청의 마지막 consumptionRecordId). 첫 요청 시 생략", example = "3", required = false)
    })
    @GetMapping("/monthly")
    public ApiResponse<HouseholdConsumptionResponse.MonthlyHistoryResponseDTO> getConsumptionRecordByMonth(@RequestParam Integer year, @RequestParam Integer month,
                                                                                                           @RequestParam(required = false) Long cursorId) {
        // 로그인 전까지 userId 1로 임시 세팅
        HouseholdConsumptionResponse.MonthlyHistoryResponseDTO response = consumptionRecordQueryService.getMonthlyConsumptionRecords(1L, year, month, cursorId);
        return ApiResponse.onSuccess(response);
    }

    // 가계부 달력 월별 소비 금액 조회
    @Operation(
            summary = "달력 - 월별 소비 금액 조회 API",
            description = "입력한 연도(year), 월(month)에 해당하는 일별 소비 총액을 조회하는 API입니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "year", description = "조회하려는 소비 연도", example = "2025", required = true),
            @Parameter(name = "month", description = "조회하려는 소비 월", example = "7", required = true),
    })
    @GetMapping("/calendar")
    public ApiResponse<HouseholdConsumptionResponse.CalendarDateAmountMapDTO> getConsumptionRecordByMonthInCalendar(@RequestParam Integer year,
                                                                                                                 @RequestParam Integer month)
    {
        // 로그인 전까지 userId 1로 임시 세팅
        HouseholdConsumptionResponse.CalendarDateAmountMapDTO response = consumptionRecordQueryService.getMonthlyConsumptionCalendar(1L, year, month);
        return ApiResponse.onSuccess(response);
    }

    // 가계부 달력 특정 일의 소비 내역 조회
    @Operation(
            summary = "달력 - 특정 날짜 소비 내역 조회 API",
            description = "입력한 연도(year), 월(month), 일(day)에 해당하는 소비 내역을 조회합니다. 해당 날짜의 소비 내역이 없다면, 빈 리스트를 반환합니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "year", description = "조회하려는 소비 연도", example = "2025", required = true),
            @Parameter(name = "month", description = "조회하려는 소비 월", example = "7", required = true),
            @Parameter(name = "day", description = "조회하려는 소비 일", example = "23", required = true),
    })
    @GetMapping("/calendar/daily")
    public ApiResponse<HouseholdConsumptionResponse.CalendarDailyConsumeDetailDTO> getConsumptionRecordByMonthAndDayInCalendar(@RequestParam Integer year, @RequestParam Integer month, @RequestParam Integer day)
    {

        // 로그인 전까지 userId 1로 임시 세팅
        HouseholdConsumptionResponse.CalendarDailyConsumeDetailDTO response = consumptionRecordQueryService.getCalendarDailyConsumptionRecordsDetail(1L, year, month, day);
        return ApiResponse.onSuccess(response);
    }
}
