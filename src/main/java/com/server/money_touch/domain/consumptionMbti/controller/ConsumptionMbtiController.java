package com.server.money_touch.domain.consumptionMbti.controller;


import com.server.money_touch.domain.consumptionMbti.dto.ConsumptionMbtiRequest;
import com.server.money_touch.domain.consumptionMbti.dto.ConsumptionMbtiResponse;
import com.server.money_touch.domain.consumptionMbti.entity.ConsumptionMbti;
import com.server.money_touch.domain.consumptionMbti.repository.ConsumptionMbtiRepository;
import com.server.money_touch.domain.consumptionMbti.service.ConsumptionMbtiService;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "소비 Mbti  페이지", description = "소비 Mbti 관한 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/consumptionMbti")
public class ConsumptionMbtiController {

    private final ConsumptionMbtiRepository consumptionMbtiRepository;
    private final ConsumptionMbtiService consumptionMbtiService;
    private final S3Manager s3Manager;

    @Operation(
            summary = "소비 Mbti 조회 API",
            description = "결과 코드(예: PTG)에 해당하는 소비 MBTI 설명, 부제, 이미지 등을 조회합니다."
    )
    @ApiSuccessCodeExample(resultClass = ConsumptionMbtiResponse.ConsumptionMbtiResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "MBTI_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @Parameters({
            @Parameter(name = "result" , description = "조회하려는 소비 MBTI", example = "PTG" , required = true)
    })
    @GetMapping("/result")
    public ApiResponse<ConsumptionMbtiResponse.ConsumptionMbtiResultDTO> getMbti(@RequestParam @NotBlank(message = "결과값은 필수입니다.") String result, HttpServletRequest request) {
        var response = consumptionMbtiService.getConsumptionMbti(result, request);
        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "소비 MBTI 저장 API",
            description = "소비 MBTI 정보와 이미지를 업로드하고 DB에 저장합니다."
    )
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> saveConsumptionMbti(
            @RequestPart("data") ConsumptionMbtiRequest.ConsumptionMbtiRequestDTO request,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            // S3 업로드
            String imageUrl = s3Manager.upload(file, "mbti");
            // 엔티티 생성 후 저장
            ConsumptionMbti mbti = new ConsumptionMbti();
            mbti.setResult(request.getResult());
            mbti.setSubtitle(request.getSubtitle());
            mbti.setDescription(request.getDescription());
            mbti.setMbtiImgUrl(imageUrl);

            consumptionMbtiRepository.save(mbti);

            return ApiResponse.onSuccess("소비 MBTI 정보가 저장되었습니다.");
        } catch (Exception e) {
            return ApiResponse.onFailure("MBTI_SAVE_FAIL", e.getMessage(), null);
        }
    }

}
