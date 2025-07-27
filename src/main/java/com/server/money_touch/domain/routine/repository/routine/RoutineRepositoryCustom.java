package com.server.money_touch.domain.routine.repository.routine;

import com.server.money_touch.domain.routine.dto.RoutineResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface RoutineRepositoryCustom {
    // 사용자의 소비 루틴 목록을 커서 기반으로 조회하고, 각 루틴에 연결된 해시태그를 함께 반환
    Slice<RoutineResponse.RoutineThumbnailDTO> findUserRoutineList(Long userId, Long cursorId, Pageable pageable);
}
