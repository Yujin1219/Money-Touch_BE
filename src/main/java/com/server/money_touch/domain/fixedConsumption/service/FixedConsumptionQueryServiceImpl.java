package com.server.money_touch.domain.fixedConsumption.service;

import com.server.money_touch.domain.fixedConsumption.converter.FixedConsumptionConverter;
import com.server.money_touch.domain.fixedConsumption.dto.FixedConsumptionResponse;
import com.server.money_touch.domain.fixedConsumption.entity.FixedConsumption;
import com.server.money_touch.domain.fixedConsumption.repository.FixedConsumptionRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FixedConsumptionQueryServiceImpl implements FixedConsumptionQueryService {

    private final FixedConsumptionRepository fixedConsumptionRepository;
    private final UserRepository userRepository;
    private static final Integer PAGE_SIZE = 10;

    // 고정비 존재 여부 검증
    @Override
    public Boolean existsFixedConsumptionById(Long fixedConsumptionId) {
        return fixedConsumptionRepository.findById(fixedConsumptionId).isPresent();
    }

    /**
     * 고정비 목록을 커서 기반으로 조회합니다.
     * - 페이지 사이즈는 고정(PAGE_SIZE)
     * - 커서 기반 페이징 방식으로, 마지막 조회된 고정비 ID 이후 데이터를 가져옵니다.
     * - 응답에는 고정비 목록, 다음 커서 ID 및 페이징 정보가 포함됩니다.
     *
     * @param userId   사용자 ID
     * @param cursorId 마지막으로 조회된 고정비 ID (null이면 첫 페이지)
     * @return FixedConsumptionCursorResultDTO (고정비 목록 + 페이징 정보)
     */
    @Override
    public FixedConsumptionResponse.FixedConsumptionCursorResultDTO getFixedConsumptions(Long userId, Long cursorId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, PAGE_SIZE);

        // 고정비 목록 조회 (Slice)
        Slice<FixedConsumption> slice = fixedConsumptionRepository
                .findFixedConsumptionsByCursor(userId, cursorId, pageable);

        // 고정비 엔티티 → DTO 변환
        List<FixedConsumptionResponse.FixedConsumptionDetailDTO> content = slice.getContent().stream()
                .map(FixedConsumptionConverter::toFixedConsumptionDetailDTO)
                .collect(Collectors.toList());

        // 다음 커서 ID 설정
        Long nextCursorId = content.isEmpty() ? null : content.get(content.size() - 1).getFixedConsumptionId();

        log.info("고정비 목록 조회(커서 기반 무한스크롤) 완료 - userId: {}, cursorId: {}, nextCursorId: {}", userId, cursorId, nextCursorId);
        return FixedConsumptionConverter.toFixedConsumptionCursorResultDTO(
                content,
                slice.hasNext(),
                nextCursorId,
                cursorId == null // 첫 페이지 여부
        );
    }
}
