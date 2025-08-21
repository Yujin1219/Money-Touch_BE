package com.server.money_touch.domain.fixedConsumption.converter;

import com.server.money_touch.domain.fixedConsumption.dto.FixedConsumptionRequest;
import com.server.money_touch.domain.fixedConsumption.dto.FixedConsumptionResponse;
import com.server.money_touch.domain.fixedConsumption.entity.FixedConsumption;
import com.server.money_touch.domain.user.entity.User;

import java.util.List;

public class FixedConsumptionConverter {

    // 고정비 엔티티 생성
    public static FixedConsumption toFixedConsumption(User user, FixedConsumptionRequest.FixedConsumptionCreateDTO requestDTO) {
        return FixedConsumption.builder()
                .user(user)
                .categoryName(requestDTO.getCategoryName())
                .fixedConsumptionAmount(requestDTO.getAmount())
                .fixedConsumptionContent(requestDTO.getContent())
                // ✅ 메모가 비어 있으면 null 로 처리
                .fixedConsumptionMemo(
                        (requestDTO.getMemo() == null || requestDTO.getMemo().isBlank())
                                ? null
                                : requestDTO.getMemo()
                )
                .appliedThisMonth(true)
                .build();
    }

    // 고정비 등록 응답 생성
    public static FixedConsumptionResponse.FixedConsumptionCreateResultDTO toFixedConsumptionCreateResultDTO(Long fixedConsumptionId) {
        return FixedConsumptionResponse.FixedConsumptionCreateResultDTO.builder()
                .fixedConsumptionId(fixedConsumptionId)
                .build();
    }

    // FixedConsumption → FixedConsumptionDetailDTO 변환
    public static FixedConsumptionResponse.FixedConsumptionDetailDTO toFixedConsumptionDetailDTO(FixedConsumption entity) {
        return FixedConsumptionResponse.FixedConsumptionDetailDTO.builder()
                .fixedConsumptionId(entity.getId())
                .categoryName(entity.getCategoryName())
                .amount(entity.getFixedConsumptionAmount())
                .content(entity.getFixedConsumptionContent())
                .build();
    }

    // FixedConsumptionDetailDTO -> FixedConsumptionCursorResultDTO로 변환
    public static FixedConsumptionResponse.FixedConsumptionCursorResultDTO toFixedConsumptionCursorResultDTO(
            List<FixedConsumptionResponse.FixedConsumptionDetailDTO> content,
            boolean hasNext,
            Long nextCursorId,
            boolean isFirst
    ) {
        return FixedConsumptionResponse.FixedConsumptionCursorResultDTO.builder()
                .fixedConsumptions(content)
                .fixedConsumptionSize(content.size())
                .isFirst(isFirst)
                .isLast(!hasNext)
                .hasNext(hasNext)
                .nextCursorId(nextCursorId)
                .build();
    }
}
