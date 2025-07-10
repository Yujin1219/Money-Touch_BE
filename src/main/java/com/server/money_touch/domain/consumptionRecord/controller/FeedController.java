package com.server.money_touch.domain.consumptionRecord.controller;

import com.server.money_touch.domain.consumptionRecord.dto.FeedRequest;
import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    // 마이페이지 - 내 피드 모아보기
    @Operation(
            summary = "내 피드 조회 API",
            description = "마이페이지에 있는 My 피드를 눌러 현재 사용자의 소비 기록 피드를 조회하는 API 입니다."
    )
//    @ApiSuccessCodeExample(resultClass = FeedResponse.FeedListResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @GetMapping("/my")
    public ApiResponse<FeedResponse.FeedListResultDTO> getMyFeed(){
        FeedResponse.FeedListResultDTO response = FeedResponse.FeedListResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }

    // 댓글 등록
    @Operation(
            summary = "댓글 등록 API",
            description = "피드에 댓글을 작성하는 API입니다. parentId가 있으면 대댓글, 없으면 일반 댓글입니다."
    )
    //    @ApiSuccessCodeExample(resultClass = CommentResponse.CommentCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_RECORD_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "PARENT_COMMENT_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "NESTED_REPLY_NOT_ALLOWED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "COMMENT_CONTENT_TOO_LONG"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "COMMENT_CONTENT_EMPTY"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @Parameters({
            @Parameter(name = "consumptionRecordId", description = "소비 기록 ID", example = "1", required = true)
    })
    @PostMapping("/{consumptionRecordId}/comment")
    public ApiResponse<FeedResponse.CommentResultDTO> createComment(
            @PathVariable Long consumptionRecordId,
            @RequestBody @Valid FeedRequest.CommentCreateDTO request
    ) {
        FeedResponse.CommentResultDTO response = FeedResponse.CommentResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }

    // 리액션 추가/변경
    @Operation(
            summary = "피드 반응 추가/변경/삭제 API",
            description = "피드에 현명해요 또는 낭비에요 반응을 추가하거나 변경 혹은 삭제하는 API입니다. 이미 반응이 있으면 변경되고, 같은 반응을 누르면 취소됩니다."
    )
//    @ApiSuccessCodeExample(resultClass = ReactionResponse.ReactionResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_RECORD_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "REACTION_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @Parameters({
            @Parameter(name = "consumptionRecordId", description = "소비 기록 ID", example = "1", required = true)
    })
    @PostMapping("/{consumptionRecordId}/reaction")
    public ApiResponse<FeedResponse.ReactionResultDTO> addOrUpdateReaction(
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long consumptionRecordId,
            @RequestBody @Valid FeedRequest.ReactionCreateDTO request
    ) {
        FeedResponse.ReactionResultDTO response = FeedResponse.ReactionResultDTO.builder().build();
        return ApiResponse.onSuccess(response);
    }
}
