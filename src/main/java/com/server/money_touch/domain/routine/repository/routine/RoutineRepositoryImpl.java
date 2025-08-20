package com.server.money_touch.domain.routine.repository.routine;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.money_touch.domain.home.dto.HomeResponse;
import com.server.money_touch.domain.routine.dto.RoutineResponse;
import com.server.money_touch.domain.routine.entity.QRoutine;
import com.server.money_touch.domain.routine.entity.QRoutineHashtag;
import com.server.money_touch.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository
public class RoutineRepositoryImpl implements RoutineRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    QRoutine routine = QRoutine.routine;
    QRoutineHashtag routineHashtag = QRoutineHashtag.routineHashtag;
    QUser user = QUser.user;

    // 사용자의 소비 루틴 목록을 커서 기반으로 조회하고, 각 루틴에 연결된 해시태그를 함께 반환
    @Override
    public Slice<RoutineResponse.RoutineThumbnailDTO> findUserRoutineList(
            Long userId, Long cursorId, Pageable pageable, int year, int month) {

        // 👉 year, month → createdMonth ("YYYY-MM") 변환
        String targetMonth = String.format("%04d-%02d", year, month);

        // 루틴 기본 정보 조회
        List<RoutineResponse.RoutineThumbnailDTO> routines = queryFactory
                .select(Projections.fields(RoutineResponse.RoutineThumbnailDTO.class,
                        routine.id.as("routineId"),
                        routine.createdAt.as("createDate"),
                        routine.routineName,
                        user.nickname,
                        routine.routineImageUrl.as("routineImgUrl"),
                        user.profileImgUrl.as("profileImgUrl")
                ))
                .from(routine)
                .join(routine.user, user)
                .where(
                        routine.user.id.eq(userId),
                        routine.createdAt.stringValue().substring(0, 7).eq(targetMonth), // ✅ createdMonth 필터링
                        cursorId != null ? routine.id.lt(cursorId) : null
                )
                .orderBy(routine.createdAt.desc(), routine.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        // 다음 페이지 여부 판별
        boolean hasNext = routines.size() > pageable.getPageSize();
        if (hasNext) routines.remove(pageable.getPageSize());

        // 루틴 ID 리스트 추출
        List<Long> routineIds = routines.stream()
                .map(RoutineResponse.RoutineThumbnailDTO::getRoutineId)
                .toList();

        // 루틴 해시태그 조회 후 Map으로 그룹핑
        Map<Long, List<String>> hashtagMap = queryFactory
                .select(routineHashtag.routine.id, routineHashtag.routineHashtagName)
                .from(routineHashtag)
                .where(routineHashtag.routine.id.in(routineIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(routineHashtag.routine.id),
                        Collectors.mapping(
                                tuple -> tuple.get(routineHashtag.routineHashtagName),
                                Collectors.toList()
                        )
                ));

        // 루틴 DTO에 해시태그 추가
        routines.forEach(r -> r.setHashtags(hashtagMap.getOrDefault(r.getRoutineId(), List.of())));

        return new SliceImpl<>(routines, pageable, hasNext);
    }

    // 전체 소비 루틴을 커서 기반 무한스크롤로 반환, 연결된 해시태그도 함께 반환
    @Override
    public Slice<RoutineResponse.RoutineListDTO> findAllRoutines(Long cursorId, Pageable pageable) {
        LocalDate today = LocalDate.now();

        // 루틴 정보 조회
        List<RoutineResponse.RoutineListDTO> routines = queryFactory
                .select(Projections.fields(RoutineResponse.RoutineListDTO.class,
                        routine.id.as("routineId"),
                        routine.createdAt.stringValue().substring(0,10).as("createDate"), // yyyy-MM-dd
                        routine.routineName,
                        user.nickname,
                        routine.routineImageUrl.as("routineImgUrl"),
                        user.profileImgUrl.as("profileImgUrl")
                ))
                .from(routine)
                .join(routine.user, user)
                .where(cursorId != null ? routine.id.lt(cursorId) : null)
                .orderBy(routine.createdAt.desc(), routine.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        // 다음 페이지 여부
        boolean hasNext = routines.size() > pageable.getPageSize();
        if (hasNext) routines.remove(pageable.getPageSize());

        // 루틴 ID 리스트
        List<Long> routineIds = routines.stream()
                .map(RoutineResponse.RoutineListDTO::getRoutineId)
                .toList();

        if (!routineIds.isEmpty()) {
            // 해시태그 조회
            Map<Long, List<String>> hashtagMap = queryFactory
                    .select(routineHashtag.routine.id, routineHashtag.routineHashtagName)
                    .from(routineHashtag)
                    .where(routineHashtag.routine.id.in(routineIds))
                    .fetch()
                    .stream()
                    .collect(Collectors.groupingBy(
                            tuple -> tuple.get(routineHashtag.routine.id),
                            Collectors.mapping(tuple -> tuple.get(routineHashtag.routineHashtagName), Collectors.toList())
                    ));

            // 해시태그 & NEW 여부 설정
            routines.forEach(r -> {
                r.setHashtags(hashtagMap.getOrDefault(r.getRoutineId(), List.of()));
                r.setNew(LocalDate.parse(r.getCreateDate()).isEqual(today));
            });
        }

        return new SliceImpl<>(routines, pageable, hasNext);
    }

    @Override
    public Slice<RoutineResponse.RoutineListDTO> searchRoutinesByKeyword(String keyword, Long cursorId, Pageable pageable) {
        LocalDate today = LocalDate.now();

        List<RoutineResponse.RoutineListDTO> routines = queryFactory
                .select(Projections.fields(RoutineResponse.RoutineListDTO.class,
                        routine.id.as("routineId"),
                        routine.createdAt.stringValue().substring(0,10).as("createDate"),
                        routine.routineName,
                        user.nickname,
                        routine.routineImageUrl.as("routineImgUrl"),
                        user.profileImgUrl.as("profileImgUrl")
                ))
                .from(routine)
                .join(routine.user, user)
                .where(
                        (keyword != null ? routine.routineName.containsIgnoreCase(keyword) : null),
                        (cursorId != null ? routine.id.lt(cursorId) : null)
                )
                .orderBy(routine.createdAt.desc(), routine.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = routines.size() > pageable.getPageSize();
        if (hasNext) routines.remove(pageable.getPageSize());

        // 해시태그 조회
        List<Long> routineIds = routines.stream().map(RoutineResponse.RoutineListDTO::getRoutineId).toList();
        if (!routineIds.isEmpty()) {
            Map<Long, List<String>> hashtagMap = queryFactory
                    .select(routineHashtag.routine.id, routineHashtag.routineHashtagName)
                    .from(routineHashtag)
                    .where(routineHashtag.routine.id.in(routineIds))
                    .fetch()
                    .stream()
                    .collect(Collectors.groupingBy(
                            tuple -> tuple.get(routineHashtag.routine.id),
                            Collectors.mapping(tuple -> tuple.get(routineHashtag.routineHashtagName), Collectors.toList())
                    ));
            routines.forEach(r -> {
                r.setHashtags(hashtagMap.getOrDefault(r.getRoutineId(), List.of()));
                r.setNew(LocalDate.parse(r.getCreateDate()).isEqual(today));
            });
        }
        return new SliceImpl<>(routines, pageable, hasNext);
    }

    @Override
    public List<HomeResponse.RoutinePreviewDTO> findTop5LatestRoutines() {
        LocalDate today = LocalDate.now();

        List<HomeResponse.RoutinePreviewDTO> routines = queryFactory
                .select(Projections.constructor(HomeResponse.RoutinePreviewDTO.class,
                        routine.id,
                        routine.routineName,
                        routine.createdAt.stringValue().substring(0,10).eq(today.toString()) // 당일 등록 여부 체크
                ))
                .from(routine)
                .orderBy(routine.createdAt.desc(), routine.id.desc())
                .limit(5)
                .fetch();

        return routines;
    }
}