package com.server.money_touch.domain.home.service;

import com.server.money_touch.domain.home.dto.HomeResponse;
import com.server.money_touch.domain.user.entity.User;

public interface HomeService {

    HomeResponse.WiseRankingResponseDTO getWeeklyWiseRanking(Long userId);

    void calculateAndSaveWeeklyWiseRanking();

    HomeResponse.ConsumptionStatisticsTopResponseDTO getTopStatistics(User user);

    HomeResponse.OtherCategoryStatisticsResponseDTO getOtherStatistics(User user);
}
