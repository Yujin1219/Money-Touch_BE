package com.server.money_touch.domain.routine.controller;

import com.server.money_touch.domain.routine.converter.RoutineConverter;
import com.server.money_touch.domain.routine.dto.RoutineRequest;
import com.server.money_touch.domain.routine.dto.RoutineResponse;
import com.server.money_touch.domain.routine.service.RoutineCommandService;
import com.server.money_touch.domain.routine.service.RoutineQueryService;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.s3.S3Manager;
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
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "가계부 소비 루틴 페이지", description = "가계부 소비 루틴에 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/house-holds/routines")
public class RoutineController {

    private final RoutineCommandService routineCommandService;
    private final RoutineQueryService routineQueryService;
    private final S3Manager s3Manager;

    // 소비 루틴 등록
    @Operation(
            summary = "소비 루틴 등록 API",
            description = "해당 API는 소비 루틴을 등록하는 기능을 제공합니다. 예산 ID는 Path Variable로 전달하며, 카테고리, 금액, 설명 등의 루틴 정보는 RequestBody로 전달합니다. " +
                    "먼저 '한 달 예산 내역 조회 API'를 통해 본인의 예산 목록을 확인한 후, 해당 예산에 포함된 모든 소비 카테고리를 기준으로 요청 데이터를 구성해 주세요. " +
                    "카테고리별 예산 금액이 기존과 다를 경우, 수정된 금액으로 요청하시면 해당 금액이 반영됩니다."
    )
    @ApiSuccessCodeExample(resultClass = RoutineResponse.RoutineCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_ALREADY_EXIST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BUDGET_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "budgetId", description = "한 달 예산 아이디", example = "1", required = true),
    })
    @PostMapping("/{budgetId}")
    public ApiResponse<RoutineResponse.RoutineCreateResultDTO> postRoutine(@Valid @RequestBody RoutineRequest.RoutineCreateDTO request,
                                                                           @PathVariable Long budgetId) {
        // 로그인 전까지 userId 1로 임시 세팅
        RoutineResponse.RoutineCreateResultDTO response = routineCommandService.saveRoutineWithRoutineHashtags(1L, budgetId, request);
        return ApiResponse.onSuccess(response);
    }

    // 내 소비 루틴 목록 조회
    @Operation(
            summary = "내 소비 루틴 목록 조회 API (커서 기반 무한스크롤)",
            description = "가계부에서 사용자가 등록한 소비 루틴 목록을 스크롤 형식으로 조회하는 API입니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameter(name = "cursorId", description = "커서(이전 요청에서 마지막 소비 루틴 아이디), 첫번째 요청일 시에는 파라미터에 포함하지 않아도 됩니다.", example = "10", required = false)
    @GetMapping("/users")
    public ApiResponse<RoutineResponse.MyRoutineListDTO> getMyRoutines(@RequestParam(required = false) Long cursorId) {
        // 로그인 전까지 userId 1로 임시 세팅
        RoutineResponse.MyRoutineListDTO response = routineQueryService.getMyRoutineList(1L, cursorId);
        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "전체 소비 루틴 리스트 조회 API",
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
    public ApiResponse<List<RoutineResponse.RoutineListDTO>> getAllRoutines() {

        // TODO: 실제 데이터로 교체 예정. 임시 더미데이터
        List<RoutineResponse.RoutineListDTO> routines = List.of(
                new RoutineResponse.RoutineListDTO(
                        1L, true, "2025-07-09", "50만원으로 한 달 살기 루틴",
                        "라인", "https://", "https://",
                        List.of("#식비절약", "#생활비")
                ),

                new RoutineResponse.RoutineListDTO(
                        2L, false, "2025-06-10", "커피값을 아끼자",
                        "오리", "https://", "https://",
                        List.of("#카페지출줄이기", "#커피절약")
                )
        );
        return ApiResponse.onSuccess(routines);
    }

    // 내 소비 루틴 상세 조회
    @Operation(
            summary = "내 소비 루틴 상세 조회 API",
            description = "가계부에서 사용자가 등록한 소비 루틴 상세 정보를 조회하는 API입니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "routineId", description = "조회하려는 소비 루틴 아이디", example = "1", required = true),
    })
    @GetMapping("/users/{routineId}")
    public ApiResponse<RoutineResponse.RoutineDetailDTO> getMyDetailRoutine(@PathVariable Long routineId) {
        // 로그인 전까지 userId 1로 임시 세팅
        RoutineResponse.RoutineDetailDTO response = routineQueryService.getUserRoutineDetail(1L, routineId);
        return ApiResponse.onSuccess(response);
    }

    // 소비 루틴 이미지 등록
    @Operation(
            summary = "소비 루틴 이미지 등록 API",
            description = "가계부에서 소비 루틴 이미지를 등록하는 API입니다. 이미지 파일을 MultipartFile 형태로 요청에 넘겨주세요."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ERROR_UPLOAD_ROUTINE_IMG"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @PostMapping(value = "/img", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<RoutineResponse.RoutineImageUrlDTO> setRoutineImage(@RequestPart("file") MultipartFile multipartFile) {
        try {
            String url = s3Manager.upload(multipartFile, "routine");
            return ApiResponse.onSuccess(RoutineConverter.toRoutineImageUrlDTO(url));
        } catch (Exception e) {
            return ApiResponse.onFailure("S3_UPLOAD_FAIL", e.getMessage(), null);
        }
    }

    @Operation(
            summary = "타인의 소비루틴 상세 조회 API",
            description = "전체 소비 루틴 리스트에서 소비 루틴 상세 정보를 조회하는 API입니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "routineId", description = "조회하려는 소비 루틴 아이디", example = "1", required = true),
    })
    @GetMapping("/list/{routineId}")
    public ApiResponse<RoutineResponse.RoutineListDetailDTO> getOtherDetailRoutine(@PathVariable Long routineId) {
        RoutineResponse.RoutineListDetailDTO response = RoutineResponse.RoutineListDetailDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "소비 루틴 예산 반영 전 수정/추가 API",
            description = "내 예산에 반영-> 네 를 클릭하면 보여주는 api 입니다."+
                    "사용자가 갖고 있는 기존 카테고리는 '카테고리별 예산', 새로운 카테고리는 '소비 루틴 카테고리' 보여줍니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "routineId", description = "조회하려는 소비 루틴 아이디", example = "1", required = true),
    })
    @GetMapping("/list/{routineId}/apply-info")
    public ApiResponse<RoutineResponse.ApplyRoutineInfoDTO> getRoutineApplyInfo(@PathVariable Long routineId){

        RoutineResponse.ApplyRoutineInfoDTO response = RoutineResponse.ApplyRoutineInfoDTO.builder().build();
        return ApiResponse.onSuccess(response);

    }

    @Operation(
            summary = "소비 루틴 예산 반영 API",
            description = "실제 예산에 반영. 새로운 카테고리는 ROUTINE_CATEGORY로 추가"
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BUDGET_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_EXCEEDED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_TOO_LOW"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @Parameters({
            @Parameter(name = "routineId", description = "소비 루틴 ID", required = true, example = "1")
    })
    @PutMapping("/list/{routineId}/apply")
    public ApiResponse<RoutineResponse.ApplyRoutineSuccessDTO> applyRoutineToBudget(
            @PathVariable Long routineId,
            @Valid @RequestBody RoutineRequest.ApplyRoutineBudgetDTO request
    ){

        RoutineResponse.ApplyRoutineSuccessDTO response = RoutineResponse.ApplyRoutineSuccessDTO.builder()
                .message("예산에 성공적으로 반영되었습니다.")
                .build();

        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "소비 루틴 검색 API",
            description = "소비 루틴 제목으로 검색합니다."
    )
    @ApiSuccessCodeExample(resultClass = RoutineResponse.RoutineListDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "keyword", description = "검색어 (루틴 이름)", example = "50만원", required = false)
    })
    @GetMapping("/list/search")
    public ApiResponse<List<RoutineResponse.RoutineListDTO>> searchRoutineList(
            @RequestParam(required = false) String keyword) {

        // TODO: 실제 데이터로 교체 예정. 임시 더미데이터
        List<RoutineResponse.RoutineListDTO> response = List.of(
                RoutineResponse.RoutineListDTO.builder()
                        .routineId(1L)
                        .isNew(true)
                        .createDate("2025-07-11")
                        .routineName("50만원으로 한 달 살기 루틴")
                        .nickname("라인")
                        .routineImgUrl("https://example.com/image.png")
                        .profileImgUrl("https://example.com/profile.png")
                        .hashtags(List.of("#절약", "#한달살기"))
                        .build()
        );

        return ApiResponse.onSuccess(response);
    }

}
