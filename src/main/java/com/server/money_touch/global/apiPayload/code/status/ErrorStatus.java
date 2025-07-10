package com.server.money_touch.global.apiPayload.code.status;

import com.server.money_touch.global.apiPayload.code.BaseErrorCode;
import com.server.money_touch.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 유저 관련 에러
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4001", "아이디와 일치하는 사용자가 없습니다."),

    // 예산 관련 에러
    BUDGET_NOT_FOUND(HttpStatus.NOT_FOUND, "BUDGET4001", "아이디와 일치하는 예산 정보를 찾을 수 없습니다."),
    TOTAL_BUDGET_EXCEEDED(HttpStatus.BAD_REQUEST, "BUDGET4002", "카테고리 예산 총합이 전체 예산을 초과합니다."),
    TOTAL_BUDGET_TOO_LOW(HttpStatus.BAD_REQUEST, "BUDGET4003", "카테고리 예산 총합이 전체 예산보다 작습니다."),

    // 소비 기록 에러
    CONSUMPTION_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "CONSUMPTION4001", "일치하는 소비기록이 존재하지 않습니다."),

    // 댓글 관련 에러
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT6001", "존재하지 않는 댓글입니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT6002", "부모 댓글을 찾을 수 없습니다."),
    NESTED_REPLY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COMMENT6003", "대댓글에는 댓글을 달 수 없습니다."),
    COMMENT_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "COMMENT6004", "댓글 내용이 너무 깁니다. (최대 300자)"),
    COMMENT_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "COMMENT6005", "댓글 내용을 입력해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}