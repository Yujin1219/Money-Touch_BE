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

    // 토큰 관련 에러
    PARSING_ERROR(HttpStatus.BAD_REQUEST, "A001", "토큰 파싱 중 오류가 발생했습니다."),

    // 예산 관련 에러
    BUDGET_NOT_FOUND(HttpStatus.BAD_REQUEST, "BUDGET4001", "아이디와 일치하는 예산이 없습니다."),
    TOTAL_BUDGET_EXCEEDED(HttpStatus.BAD_REQUEST, "BUDGET4002", "카테고리 예산 총합이 전체 예산을 초과합니다."),
    TOTAL_BUDGET_TOO_LOW(HttpStatus.BAD_REQUEST, "BUDGET4003", "카테고리 예산 총합이 전체 예산보다 작습니다."),
    BUDGET_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "BUDGET4004", "한 달 예산 등록 횟수를 초과하였습니다."),
    BUDGET_NOT_EXIST(HttpStatus.NOT_FOUND, "BUDGET_4041", "이번달에 등록된 예산이 없습니다."),
    ROUTINE_CATEGORY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "BUDGET_403", "소비 루틴 카테고리는 소비 루틴을 등록한 경우나, 타인의 소비 루틴을 내 예산에 반영한 경우만 설정할 수 있습니다."),
    BUDGE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "BUDGET4011", "접근 권한이 없는 예산 아이디입니다. 예산 아이디 조회를 통해 올바른 예산 아이디로 구성해주세요."),

    // 소비 MBTI 관련 에러
    MBTI_NOT_FOUND(HttpStatus.BAD_REQUEST, "MBTI4001", "해당하는 소비 MBTI가 없습니다."),

    // 소비 카테고리 관련 에러
    CONSUMPTION_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CONSUMPTION_CATEGORY4001", "해당 카테고리 테이블을 찾을 수 없습니다."),
    CONSUMPTION_CATEGORY_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "CONSUMPTION_CATEGORY4001", "해당 카테고리 타입을 찾을 수 없습니다."),
    CONSUMPTION_CATEGORY_NAME_NOT_FOUND(HttpStatus.NOT_FOUND, "CONSUMPTION_CATEGORY4002", "해당 카테고리 이름을 찾을 수 없습니다."),
    CONSUMPTION_CATEGORY_NAME_MISSING_IN_REQUEST(HttpStatus.NOT_FOUND, "CONSUMPTION_CATEGORY4003", "요청에 누락된 소비 카테고리 항목이 있습니다. (예산에 등록된 소비 카테고리 중 일부가 요청에 포함되지 않았습니다."),

    // 소비 기록 에러
    CONSUMPTION_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "CONSUMPTION4001", "일치하는 소비기록이 존재하지 않습니다."),
    FORBIDDEN_ACCESS_ON_PRIVATE_FEED(HttpStatus.FORBIDDEN, "CONSUMPTION4002", "비공개 피드엔 접근할 수 없습니다."),

    // 댓글 관련 에러
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT4001", "존재하지 않는 댓글입니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT4002", "부모 댓글을 찾을 수 없습니다."),
    NESTED_REPLY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COMMENT4003", "대댓글에는 댓글을 달 수 없습니다."),

    // 배지 관련 에러
    BADGE_NOT_FOUND(HttpStatus.NOT_FOUND, "BADGE4001", "존재하지 않는 배지 입니다."),
    BADGE_NOT_EARNED(HttpStatus.FORBIDDEN, "BADGE4002", "획득하지 않은 배지입니다."),

    // 고정비 관련 에러
    FIXED_CONSUMPTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "FIXED_CONSUMPTION4001", "아이디와 일치하는 고정비가 없습니다."),

    // 총 소비 관련 에러
    TOTAL_CONSUMPTION_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "TOTAL_CONSUMPTION5001", "총 소비 데이터가 존재하지 않습니다. 관리자에게 문의 바랍니다."),

    // 소비 루틴 관련 에러
    ROUTINE_NOT_FOUND(HttpStatus.BAD_REQUEST, "ROUTINE4001", "아이디와 일치하는 소비 루틴이 없습니다."),
    ROUTINE_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "ROUTINE4002", "한 달 소비 루틴 등록 횟수를 초과하였습니다."),
    ROUTINE_ALREADY_APPLIED(HttpStatus.BAD_REQUEST, "ROUTINE4003", "한 달 소비 루틴 가져오기 횟수를 초과하였습니다."),
    ROUTINE_PREVIEW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "ROUTINE4004", "자신의 소비 루틴은 미리보기로 가져올 수 없습니다."),
    ERROR_UPLOAD_ROUTINE_IMG(HttpStatus.INTERNAL_SERVER_ERROR, "ROUTINE5001", "소비 루틴 이미지 등록에 실패하였습니다."),


    // 알림 관련 에러
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4001", "존재하지 않는 알림입니다."),
    NO_PERMISSION_FOR_NOTIFICATION(HttpStatus.FORBIDDEN, "NOTIFICATION4002", " 해당 알림에 접근할 권한이 없습니다."),
    ALREADY_READ_NOTIFICATION(HttpStatus.CONFLICT, "NOTIFICATION4003", "이미 읽음 처리된 알림입니다.");

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