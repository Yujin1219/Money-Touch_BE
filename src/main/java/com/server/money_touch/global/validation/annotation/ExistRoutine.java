package com.server.money_touch.global.validation.annotation;

import com.server.money_touch.global.validation.validator.RoutineExistValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

// 소비 루틴 존재 검증
@Documented
@Constraint(validatedBy = RoutineExistValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistRoutine {
    String message() default "아이디와 일치하는 소비 루틴이 없습니다."; // 기본 에러 메시지

    Class<?>[] groups() default {}; // 유효성 검사 그룹

    Class<? extends Payload>[] payload() default {}; // 메타데이터 전달용
}