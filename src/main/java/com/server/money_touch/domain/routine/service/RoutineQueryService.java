package com.server.money_touch.domain.routine.service;

import com.server.money_touch.domain.routine.dto.RoutineResponse;
import com.server.money_touch.global.validation.annotation.ExistRoutine;
import com.server.money_touch.global.validation.annotation.ExistUser;

public interface RoutineQueryService {
    // 소비 루틴 존재 여부 검증
    Boolean existsRoutineById(Long routineId);

    // 내 소비 루틴 상세 조회
    RoutineResponse.RoutineDetailDTO getUserRoutineDetail(@ExistUser Long userId, @ExistRoutine Long routineId);

    // 내 소비 루틴 목록 조회 (커서 기반 무한스크롤)
    RoutineResponse.MyRoutineListDTO getMyRoutineList(@ExistUser Long userId, int year, int month, Long cursorId);

    // 전체 소비 루틴 목록 조회 (커서 기반 무한스크롤)
    RoutineResponse.AllRoutineListDTO getAllRoutineList(Long cursorId);

    // 소비 루틴 상세 조회
    RoutineResponse.RoutineListDetailDTO getOtherRoutineDetail(@ExistUser Long userId, @ExistRoutine Long routineId);

    // 검색 키워드로 소비 루틴 목록 조회(커서 기반 무한스크롤)
    RoutineResponse.AllRoutineListDTO searchRoutineList(String keyword, Long cursorId);

    // 타인의 소비 루틴을 내 예산에 반영 시 미리보기
    RoutineResponse.ApplyRoutineInfoDTO getRoutineApplyInfo(@ExistUser Long userId, @ExistRoutine Long routineId);
}
