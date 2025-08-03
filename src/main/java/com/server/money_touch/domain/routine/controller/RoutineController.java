package com.server.money_touch.domain.routine.controller;

import com.server.money_touch.domain.routine.converter.RoutineConverter;
import com.server.money_touch.domain.routine.dto.RoutineRequest;
import com.server.money_touch.domain.routine.dto.RoutineResponse;
import com.server.money_touch.domain.routine.service.RoutineCommandService;
import com.server.money_touch.domain.routine.service.RoutineQueryService;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import com.server.money_touch.global.config.jwt.TokenProvider;
import com.server.money_touch.global.s3.S3Manager;
import com.server.money_touch.global.utils.AuthUtil;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import com.server.money_touch.global.validation.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.server.money_touch.global.apiPayload.code.status.ErrorStatus.CONSUMPTION_CATEGORY_NAME_MISSING_IN_REQUEST;

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
    private final AuthUtil authUtil;

    // 소비 루틴 등록
    @Operation(
            summary = "소비 루틴 등록 API",
            description = "해당 API는 소비 루틴을 등록하는 기능을 제공합니다. 예산 ID는 Path Variable로 전달하며, 카테고리, 금액, 설명 등의 루틴 정보는 RequestBody로 전달합니다. " +
                    "먼저 '한 달 예산 내역 조회 API'를 통해 본인의 예산 목록을 확인한 후, 해당 예산에 포함된 모든 소비 카테고리를 기준으로 요청 데이터를 구성해 주세요. " +
                    "기존 예산에 저장되어 있는 카테고리 이외에 새로운 카테고리와 금액을 등록할 경우, 요청 데이터에 포함해주세요." +
                    "카테고리별 예산 금액이 기존과 다를 경우, 수정된 금액으로 요청하시면 해당 금액이 반영됩니다."
    )
    @ApiSuccessCodeExample(resultClass = RoutineResponse.RoutineCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_ALREADY_EXIST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BUDGET_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_CATEGORY_NAME_MISSING_IN_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "budgetId", description = "한 달 예산 아이디", example = "1", required = true),
    })
    @PostMapping("/{budgetId}")
    public ApiResponse<RoutineResponse.RoutineCreateResultDTO> postRoutine(@Valid @RequestBody RoutineRequest.RoutineCreateDTO request,
                                                                           @PathVariable Long budgetId, HttpServletRequest servletrequest) {

        Long userId = authUtil.getUserIdFromRequest(servletrequest);

        RoutineResponse.RoutineCreateResultDTO response = routineCommandService.saveRoutineWithRoutineHashtags(userId, budgetId, request);
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
    public ApiResponse<RoutineResponse.MyRoutineListDTO> getMyRoutines(@RequestParam(required = false) Long cursorId, HttpServletRequest servletRequest) {
        Long userId = authUtil.getUserIdFromRequest(servletRequest);
        RoutineResponse.MyRoutineListDTO response = routineQueryService.getMyRoutineList(userId, cursorId);
        return ApiResponse.onSuccess(response);
    }

    // 전체 소비 루틴 목록 조회
    @Operation(
            summary = "전체 소비 루틴 리스트 조회 API (커서 기반 무한스크롤)",
            description = "최신순으로 전체 소비 루틴을 조회합니다. 당일 등록은 NEW 표시를 위해 true로 전달합니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_NOT_FOUND"),
    })
    @Parameter(name = "cursorId", description = "커서(이전 요청에서 마지막 소비 루틴 아이디), 첫번째 요청일 시에는 파라미터에 포함하지 않아도 됩니다.", example = "10", required = false)

    @GetMapping("/list")
    public ApiResponse<RoutineResponse.AllRoutineListDTO> getAllRoutines(@RequestParam(required = false) Long cursorId) {

        RoutineResponse.AllRoutineListDTO response = routineQueryService.getAllRoutineList(cursorId);
        return ApiResponse.onSuccess(response);
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
    public ApiResponse<RoutineResponse.RoutineDetailDTO> getMyDetailRoutine(@PathVariable Long routineId, HttpServletRequest servletRequest) {
        Long userId = authUtil.getUserIdFromRequest(servletRequest);
        RoutineResponse.RoutineDetailDTO response = routineQueryService.getUserRoutineDetail(userId, routineId);
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

    // 타인의 소비루틴 상세 조회
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
    public ApiResponse<RoutineResponse.RoutineListDetailDTO> getOtherDetailRoutine(@PathVariable Long routineId, HttpServletRequest servletrequest) {

        Long userId = authUtil.getUserIdFromRequest(servletrequest);

        RoutineResponse.RoutineListDetailDTO response = routineQueryService.getOtherRoutineDetail(userId, routineId);
        return ApiResponse.onSuccess(response);
    }

    // 소비 루틴 예산 반영 전 수정/추가(미리 보기)_
    @Operation(
            summary = "소비 루틴 예산 반영 전 수정/추가(미리보기) API",
            description = "해당 API는 소비 루틴을 예산에 반영하기 전에, 적용 시 변경될 카테고리별 금액을 미리 확인할 수 있도록 합니다. " +
                    "'내 예산에 반영 → 네'를 선택하기 전, 기존 예산에 포함된 카테고리는 '카테고리별 예산'으로, 새롭게 추가되는 카테고리는 '소비 루틴 카테고리'로 구분하여 보여줍니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_ALREADY_APPLIED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "routineId", description = "가져오려는 소비 루틴 아이디", example = "1", required = true),
    })
    @GetMapping("/list/{routineId}/apply-info")
    public ApiResponse<RoutineResponse.ApplyRoutineInfoDTO> getRoutineApplyInfo(@PathVariable Long routineId, HttpServletRequest servletRequest) {

        Long userId = authUtil.getUserIdFromRequest(servletRequest);
        RoutineResponse.ApplyRoutineInfoDTO response = routineQueryService.getRoutineApplyInfo(userId, routineId);
        return ApiResponse.onSuccess(response);

    }

    // 소비 루틴 예산 반영
    @Operation(
            summary = "타인의 소비 루틴을 내 예산에 반영 API",
            description = "해당 API는 사용자가 직접 예산을 등록하거나 수정하는 것이 아닌, 타인의 소비 루틴을 자신의 예산에 반영할 때 사용합니다. " +
                    "반영 전에는 '소비 루틴 예산 미리보기 API'를 통해 카테고리 관련 요청 데이터를 구성합니다." +
                    "필요 시 금액을 수정하거나 항목을 추가할 수 있습니다. " +
                    "만약 새로운 소비 카테고리를 추가한 경우에는 customCategoryBudgets에 타입을 CUSTOM으로 지정하여 포함시켜야 합니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "BUDGET_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_EXCEEDED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOTAL_BUDGET_TOO_LOW"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "ROUTINE_ALREADY_APPLIED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @Parameters({
            @Parameter(name = "budgetId", description = "현재 월의 예산 아이디", example = "1", required = true),
            @Parameter(name = "routineId", description = "가져오려는 타인의 소비 루틴 ID", example = "1", required = true)
    })
    @PatchMapping("/list/{routineId}/apply")
    public ApiResponse<String> applyRoutineToBudget(
            @RequestParam Long budgetId,
            @RequestParam Long routineId,
            @Valid @RequestBody RoutineRequest.ApplyRoutineBudgetDTO request,
            HttpServletRequest servletRequest
    ){
        Long userId = authUtil.getUserIdFromRequest(servletRequest);
        routineCommandService.applyRoutineToBudget(userId, budgetId, routineId, request);
        return ApiResponse.onSuccess("예산에 성공적으로 반영되었습니다.");
    }

    // 소비 루틴 검색 (커서 기반 무한 스크롤)
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
            @Parameter(name = "keyword", description = "검색어 (루틴 이름)", example = "50만원", required = false),
            @Parameter(name = "cursorId", description = "커서(이전 요청에서 마지막 소비 루틴 아이디), 첫번째 요청일 시에는 파라미터에 포함하지 않아도 됩니다.", example = "10", required = false)

    })
    @GetMapping("/list/search")
    public ApiResponse<RoutineResponse.AllRoutineListDTO> searchRoutineList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long cursorId) {
        RoutineResponse.AllRoutineListDTO response = routineQueryService.searchRoutineList(keyword, cursorId);
        return ApiResponse.onSuccess(response);
    }

}
