package com.server.money_touch.domain.consumptionRecord.controller;

import com.server.money_touch.domain.consumptionRecord.dto.FeedRequest;
import com.server.money_touch.domain.consumptionRecord.dto.FeedResponse;
import com.server.money_touch.domain.consumptionRecord.enums.FeedSortType;
import com.server.money_touch.domain.consumptionRecord.enums.MyFeedViewType;
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
import jakarta.servlet.http.HttpServletResponse;
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
            description = "해당 피드를 눌렀을 때 피드의 상세 정보를 조회하며 조회수를 증가시키는 API입니다.\n" +
                    " - 피드 상세 조회 시 자동으로 조회수가 1 증가합니다.\n" +
                    " - 쿠키를 사용하여 24시간 동안 중복 조회수 증가를 방지합니다.\n" +
                    " - 동일한 사용자가 같은 게시글을 24시간 내 재조회해도 조회수가 중복으로 증가하지 않습니다."
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
    public ApiResponse<FeedResponse.FeedDetailResultDTO> getFeedDetail(
            @PathVariable Long consumptionRecordId, HttpServletRequest servletrequest, HttpServletResponse servletresponse) {

        Long userId = authUtil.getUserIdFromRequest(servletrequest);
        FeedResponse.FeedDetailResultDTO response = feedService.getFeedDetail(userId, consumptionRecordId, servletrequest, servletresponse);
        return ApiResponse.onSuccess(response);
    }

    // 마이페이지 - 내 피드 모아보기
    @Operation(
            summary = "내 피드 조회 API",
            description = "마이페이지에 있는 My 피드를 눌러 현재 사용자의 소비 기록 피드를 조회하는 API 입니다." +
                    "- viewMode에 따라 카드형(CARD) 또는 리스트형(LIST)으로 데이터를 반환합니다. \n" +
                    "   - 카드형(CARD): pagesize : 20" +
                    "   - 리스트형(LIST): pagesize : 5" +
                    "- 커서 기반 무한 스크롤 방식으로 페이징되며, cursorId가 없는 경우 첫 페이지로 간주됩니다."
    )
//    @ApiSuccessCodeExample(resultClass = FeedResponse.FeedListResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @GetMapping("/my")
    public ApiResponse<FeedResponse.MyFeedListResultDTO> getMyFeed(
            HttpServletRequest servletrequest,
            @RequestParam(name = "viewMode") MyFeedViewType viewMode,
            @RequestParam(name = "cursorId", required = false) Long cursorId
    ){
        Long userId = authUtil.getUserIdFromRequest(servletrequest);
        FeedResponse.MyFeedListResultDTO response =feedService.getMyFeedsByCursor(userId, viewMode, cursorId);
        return ApiResponse.onSuccess(response);
    }

    // 마이페이지 - 내 피드 모아보기
    @Operation(
            summary = "유저 닉네임 기반 게시글 검색 API",
            description = "유저 닉네임에 키워드가 포함된 게시글을 검색하는 API입니다.\n"
                    + "- 검색 대상: 공개된 소비 기록만 조회됩니다.\n"
                    + "- 검색 조건: 사용자 닉네임에 키워드가 포함될 경우 해당 사용자의 게시글을 반환합니다.\n"
                    + "- 정렬: 최신순으로 정렬됩니다.\n"
                    + "- 커서 기반 무한스크롤 방식 지원 (cursorId 파라미터 사용)\n\n"
                    + "✅ 예시\n"
                    + "- keyword = \"유\" → 닉네임이 \"유진\", \"김유라\", \"유리\" 등인 유저의 게시글 반환\n"
                    + "- cursorId = null → 첫 페이지 요청\n"
                    + "- cursorId = 18 → ID가 18보다 작은 게시글부터 다음 페이지 조회"
    )

//    @ApiSuccessCodeExample(resultClass = FeedResponse.FeedListResultDTO.class)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @GetMapping("/search")
    public ApiResponse<FeedResponse.FeedListResultDTO> searchFeeds(
            HttpServletRequest servletrequest,
            @RequestParam String keyword,
            @RequestParam(required = false) Long cursorId
    ){
        Long userId = authUtil.getUserIdFromRequest(servletrequest);
        FeedResponse.FeedListResultDTO response = feedService.searchFeedsByUserNickname(keyword, cursorId, userId);
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
            @RequestBody @Valid FeedRequest.CommentCreateDTO request, HttpServletRequest servletrequest
    ) {
        Long userId = authUtil.getUserIdFromRequest(servletrequest);
        FeedResponse.CommentResultDTO response = commentService.createComment(userId, consumptionRecordId, request);
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
            @PathVariable Long consumptionRecordId,  HttpServletRequest servletrequest
    ) {
        Long userId = authUtil.getUserIdFromRequest(servletrequest);
        List<FeedResponse.CommentListDTO> response = commentService.getCommentList(userId, consumptionRecordId);
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
            @PathVariable Long commentId, HttpServletRequest servletrequest
    ) {
        Long userId = authUtil.getUserIdFromRequest(servletrequest);
        FeedResponse.CommentLikeResultDTO response = commentLikeService.addOrRemoveLike(userId, commentId);
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
            @PathVariable Long consumptionRecordId,
            @RequestBody @Valid FeedRequest.ReactionCreateDTO request, HttpServletRequest servletrequest
    ) {
        Long userId = authUtil.getUserIdFromRequest(servletrequest);
        FeedResponse.ReactionResultDTO response = reactionService.addOrUpdateReaction(userId, consumptionRecordId, request);
        return ApiResponse.onSuccess(response);
    }
}
