package com.server.money_touch.domain.notification.controller;

import com.server.money_touch.domain.notification.dto.NotificationResponse;
import com.server.money_touch.domain.notification.service.NotificationService;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

    @Tag(name = "알림 페이지", description = "알림 조회 API")
    @Slf4j
    @Validated
    @RequiredArgsConstructor
    @RestController
    @RequestMapping("/api/notification")
    public class NotificationController {

        private final NotificationService notificationService;

        // 전체 알림 조회
        @Operation(
                summary = "전체 알림 조회 API",
                description = "커서 기반 무한스크롤로 사용자의 모든 알림목록을 조회하는 API입니다."
        )
//        @ApiSuccessCodeExample(resultClass = NotificationResponse.NotificationListDTO.class)
        @ApiErrorCodeExamples({
                @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
                @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
                @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
        })
        @Parameters({
                @Parameter(name = "cursor", description = "커서 ID (첫 조회시 null)", example = "123")
        })
        @GetMapping("/list")
        public ApiResponse<NotificationResponse.NotificationListDTO> getNotificationList(
                @Parameter(description = "커서 ID (첫 조회시 null)", example = "10")
                @RequestParam(required = false) Long cursor) {

            log.info("알림 목록 조회 요청 - cursor: {}", cursor);

            // userId 임시로 1로 지정 (추후 JWT 토큰에서 추출)
            NotificationResponse.NotificationListDTO response =
                    notificationService.getNotificationsByCursor(1L, cursor);

            return ApiResponse.onSuccess(response);
        }

        @Operation(
                summary = "알림 읽음 처리 API",
                description = "특정 알림을 읽음 처리합니다. 프론트에서 알림을 클릭할 때 호출됩니다."
        )
//        @ApiSuccessCodeExample(resultClass = NotificationResponse.NotificationReadResultDTO.class)
        @ApiErrorCodeExamples({
                @ApiErrorCodeExample(value = ErrorStatus.class, name = "NOTIFICATION_NOT_FOUND"),
                @ApiErrorCodeExample(value = ErrorStatus.class, name = "NO_PERMISSION_FOR_NOTIFICATION"),
                @ApiErrorCodeExample(value = ErrorStatus.class, name = "ALREADY_READ_NOTIFICATION"),
                @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
                @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
                @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
        })
        @Parameters({
                @Parameter(name = "notificationId", description = "읽음 처리되는 알림 아이디", example = "1", required = true)
        })
        @PatchMapping("/{notificationId}/read")
        public ApiResponse<NotificationResponse.NotificationReadResultDTO> readNotification(
                @PathVariable Long notificationId
        ) {
            log.info("알림 읽음 처리 요청 - notificationId: {}", notificationId);

            // userId 임시로 1로 지정 (추후 JWT 토큰에서 추출)
            NotificationResponse.NotificationReadResultDTO response =notificationService.markAsRead(1L, notificationId);
            return ApiResponse.onSuccess(response);
        }
    }

