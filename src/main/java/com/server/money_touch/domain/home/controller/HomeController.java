package com.server.money_touch.domain.home.controller;

import com.server.money_touch.domain.home.dto.HomeResponse;
import com.server.money_touch.domain.home.service.HomeService;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.GeneralException;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "홈 페이지", description = "홈 화면에 있는 기능 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;
    private final UserRepository userRepository;

    @Operation(
            summary = "소비 통계 조회 API",
            description = "이번 달 상위 5개 소비 카테고리(카테고리명, 퍼센트) + 그외 여부 + 그외(hasothers) 퍼센트(없으면 0.0) + 최다 소비 항목 반환.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })

    @GetMapping("/statistics")
    public ApiResponse<HomeResponse.ConsumptionStatisticsTopResponseDTO> getTopStatistics(){

        User user = userRepository.findById(1L)
                .orElseThrow(()-> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        return ApiResponse.onSuccess(homeService.getTopStatistics(user));

    }

    @Operation(
            summary = "그외 카테고리 통계 조회 API",
            description = "'그 외'를 클릭했을 때 상위 5개를 제외한 카테고리들의 소비 퍼센트 반환"+
                            "반올림으로 인해 그외 카테고리 퍼센트 합계시 '그외'의 전체 퍼센트와 0.1% 차이가 발생할 수 있습니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @GetMapping("/statistics/others")
    public ApiResponse<HomeResponse.OtherCategoryStatisticsResponseDTO> getOtherStatistics(){

        User user = userRepository.findById(1L)
                .orElseThrow(()-> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        return ApiResponse.onSuccess(homeService.getOtherStatistics(user));

    }

    @Operation(
            summary = "소비왕 랭킹 조회 API",
            description = "지난 주 기준 현명해요 수가 가장 많은 유저 10명 + 내 순위를 반환합니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @GetMapping("/ranking")
    public ApiResponse<HomeResponse.WiseRankingResponseDTO> getWiseRanking() {

        // 임시 유저 지정
        return ApiResponse.onSuccess(homeService.getWeeklyWiseRanking(1L));

    }

    @Operation(
            summary = "소비 루틴 목록 5개 조회 API",
            description = "사용자들이 등록한 소비 루틴들을 조회순 5개 보여줍니다.당일 등록된 루틴은 NEW 표시가 추가됩니다."+
            "Try it out -> Execute 로 리스트 확인 가능합니다. " +
            "임시 더미데이터 입력한 상태")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_NOT_FOUND"),
    })
    @GetMapping("/routinesPreview")
    public ApiResponse<List<HomeResponse.RoutinePreviewDTO>> getRoutinePreviews() {

        // TODO: 실제 데이터로 교체 예정. 임시 더미데이터
        List<HomeResponse.RoutinePreviewDTO> routines = List.of(
                new HomeResponse.RoutinePreviewDTO(
                        1L, "https://",
                        "50만원으로 한 달 살기 루틴",
                        true
                ),
                new HomeResponse.RoutinePreviewDTO(
                        2L,"https://",
                        "배달 끊고 집밥 먹기 예산",
                         true
                ),
                new HomeResponse.RoutinePreviewDTO(
                        3L, "https://",
                        "커피값을 아끼자",
                        false
                ),
                new HomeResponse.RoutinePreviewDTO(
                        4L, "https://",
                        "쇼핑은 10만원만",
                        false
                ),
                new HomeResponse.RoutinePreviewDTO(
                        5L, "https://",
                        "줄줄 새는 고정비 확인하기",
                        false
                )
        );

        return ApiResponse.onSuccess(routines);
    }

    @Operation(
            summary = "[관리자용] 랭킹 수동 갱신 API",
            description = "지난 주 소비 기록 기준으로 랭킹을 수동으로 계산, 저장합니다.")
    @GetMapping("/ranking/refresh")
    public ApiResponse<String> refreshWiseRankingManually() {
        homeService.calculateAndSaveWeeklyWiseRanking();
        return ApiResponse.onSuccess("주간 랭킹 수동 갱신 완료");
    }

}
