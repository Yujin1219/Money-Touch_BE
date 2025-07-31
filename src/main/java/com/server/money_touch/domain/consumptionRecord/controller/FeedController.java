package com.server.money_touch.domain.consumptionRecord.controller;

import com.server.money_touch.domain.consumptionRecord.dto.FeedRequest;
import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.enums.FeedSortType;
import com.server.money_touch.domain.consumptionRecord.service.comment.CommentLikeService;
import com.server.money_touch.domain.consumptionRecord.service.comment.CommentService;
import com.server.money_touch.domain.consumptionRecord.service.feed.FeedService;
import com.server.money_touch.domain.consumptionRecord.service.reaction.ReactionService;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.utils.AuthUtil;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "피드 페이지", description = "피드 조회 API")
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final ReactionService reactionService;
    private final CommentService commentService;
    private final CommentLikeService commentLikeService;
    private final FeedService feedService;
    private final AuthUtil authUtil;

    // 피드 홈 (피드 리스트) 조회
    @Operation(
            summary = "피드 홈(피드 리스트) API",
            description = """
            공개된 소비기록(가계부에만 등록하지 않은 소비기록) 피드를 커서 기반 무한스크롤 방식으로 조회합니다.

            정렬 방식:
            - RECENT (기본값): 가장 최근에 생성된 게시물부터 정렬됩니다. (ID 내림차순)
            - POPULAR: 조회수가 높은 게시물부터 정렬됩니다. 조회수가 같을 경우 ID가 더 큰 게시물이 우선입니다.

            커서 방식:
            - 최신순(RECENT)일 경우 → cursorId 사용
            - 조회수순(POPULAR)일 경우 → cursorViewCount + cursorId 함께 사용
            """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
    })
    @GetMapping("/home")
    public ApiResponse<FeedResponse.FeedListResultDTO> getFeedList(
            HttpServletRequest request,
            @RequestParam(name = "sortType", defaultValue = "RECENT") FeedSortType sortType,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "cursorViewCount", required = false) Integer cursorViewCount
    ) {
        Long userId = authUtil.getUserIdFromRequest(request);

        FeedResponse.FeedListResultDTO response =
                feedService.getFeedsByCursor(userId, sortType, cursorId, cursorViewCount);

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
        FeedResponse.FeedDetailResultDTO response = feedService.getFeedDetail(1L, consumptionRecordId);
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
            description = "피드에 댓글을 작성하는 API입니다. parentId가 있으면 대댓글, 없으면 일반 댓글입니다. 공개된 소비 기록(가계부에만 등록하지 않은 소비기록)에 한해서만 가능합니다."
    )
    //    @ApiSuccessCodeExample(resultClass = CommentResponse.CommentCreateResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_RECORD_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "PARENT_COMMENT_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "FORBIDDEN_ACCESS_ON_PRIVATE_FEED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "NESTED_REPLY_NOT_ALLOWED"),
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
        FeedResponse.CommentResultDTO response = commentService.createComment(1L, consumptionRecordId, request);
        return ApiResponse.onSuccess(response);
    }

    // 댓글 조회
    @Operation(summary = "댓글 목록 조회 API", description = "특정 소비기록의 댓글과 대댓글 목록을 계층 구조로 반환합니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_RECORD_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "FORBIDDEN_ACCESS_ON_PRIVATE_FEED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @Parameters({
            @Parameter(name = "consumptionRecordId", description = "소비 기록 ID", example = "1", required = true)
    })
    @GetMapping("/{consumptionRecordId}/comments")
    public ApiResponse<List<FeedResponse.CommentListDTO>> getComments(
            @PathVariable Long consumptionRecordId
    ) {
        List<FeedResponse.CommentListDTO> response = commentService.getCommentList(1L, consumptionRecordId);
        return ApiResponse.onSuccess(response);
    }


    // 댓글 좋아요
    @Operation(
            summary = "댓글 좋아요 추가/취소 API",
            description = "댓글에 좋아요를 누르거나 취소하는 API입니다. 같은 사용자가 같은 댓글에 중복으로 좋아요를 누를 수 없습니다. 다시 누르면 취소됩니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "COMMENT_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "FORBIDDEN_ACCESS_ON_PRIVATE_FEED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @Parameters({
            @Parameter(name = "commentId", description = "댓글 ID", required = true, example = "3")
    })
    @PostMapping("/comment/{commentId}/like")
    public ApiResponse<FeedResponse.CommentLikeResultDTO> addOrRemoveCommentLike(
//          @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long commentId
    ) {
        FeedResponse.CommentLikeResultDTO response = commentLikeService.addOrRemoveLike(1L, commentId);
        return ApiResponse.onSuccess(response);
    }

    // 리액션 추가/변경
    @Operation(
            summary = "피드 반응 추가/변경/삭제 API",
            description = "피드에 현명해요 또는 낭비에요 반응을 추가하거나 변경 혹은 삭제하는 API입니다. 이미 반응이 있으면 변경되고, 같은 반응을 누르면 취소됩니다. 공개된 소비 기록(가계부에만 등록하지 않은 소비기록)에 한해서만 가능합니다."
    )
//    @ApiSuccessCodeExample(resultClass = ReactionResponse.ReactionResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_RECORD_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "FORBIDDEN_ACCESS_ON_PRIVATE_FEED"),
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
        FeedResponse.ReactionResultDTO response = reactionService.addOrUpdateReaction(1L, consumptionRecordId, request);
        return ApiResponse.onSuccess(response);
    }

    // 피드 조회수 증가
    @Operation(
            summary = "피드 조회수 증가 API",
            description = "피드를 조회할 때 조회수를 증가시키는 API입니다. 중복 조회도 가능합니다(중복 제한으로 수정이 필요할 것 같으면 말해주세요). 공개된 소비 기록(가계부에만 등록하지 않은 소비기록)에 한해서만 가능합니다."
    )
//    @ApiSuccessCodeExample(resultClass = FeedResponse.ViewCountResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "CONSUMPTION_RECORD_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @Parameters({
            @Parameter(name = "consumptionRecordId", description = "소비 기록 ID", example = "1", required = true)
    })
    @PatchMapping("/{consumptionRecordId}/view")
    public ApiResponse<FeedResponse.ViewCountResultDTO> increaseFeedViewCount(
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long consumptionRecordId
    ) {
        FeedResponse.ViewCountResultDTO response =feedService.increaseFeedViewCount(1L, consumptionRecordId);
        return ApiResponse.onSuccess(response);
    }
}
