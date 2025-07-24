package com.server.money_touch.domain.badge.converter;

import com.server.money_touch.domain.badge.dto.BadgeResponse;
import com.server.money_touch.domain.badge.entity.Badge;
import com.server.money_touch.domain.badge.entity.UserBadge;

import java.util.List;
import java.util.stream.Collectors;

public class BadgeConverter {

    // List<UserBadge> -> List<BadgeDetailResultDTO>
    public static List<BadgeResponse.BadgeDetailResultDTO> toBadgeDetailList(List<UserBadge> userBadges){

        return userBadges.stream()
                .map(BadgeConverter::toBadgeDetailDTO)
                .collect(Collectors.toList());
    }

    // UserBadge -> Badge 상세 DTO
    public static BadgeResponse.BadgeDetailResultDTO toBadgeDetailDTO(UserBadge userBadge){

        Badge badge = userBadge.getBadge();

        return BadgeResponse.BadgeDetailResultDTO.builder()
                .badgeId(badge.getId())
                .name(badge.getName())
                .imageUrl(badge.getImageUrl())
                .description(badge.getDescription())
                .build();
    }

    // List<UserBadge> -> MyBadgeListResultDTO 응답 DTO
    public static BadgeResponse.MyBadgeListResultDTO toMyBadgeListDTO(List<UserBadge> userBadges){

        List<BadgeResponse.BadgeDetailResultDTO> badgeList = toBadgeDetailList(userBadges);

        return BadgeResponse.MyBadgeListResultDTO.builder()
                .badges(badgeList)
                .build();
    }

    // UserBadge → 대표 배지 응답 DTO
    public static BadgeResponse.RepresentativeBadgeResultDTO toRepresentativeBadgeDTO(UserBadge userBadge) {
        Badge badge = userBadge.getBadge();
        return BadgeResponse.RepresentativeBadgeResultDTO.builder()
                .badgeId(badge.getId())
                .badgeName(badge.getName())
                .badgeImageUrl(badge.getImageUrl())
                .badgeDescription(badge.getDescription())
                .build();
    }
}
