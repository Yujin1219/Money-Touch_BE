package com.server.money_touch.domain.home.service;

import com.server.money_touch.domain.home.dto.HomeResponse;

public interface HomeService {

    HomeResponse.WiseRankingResponseDTO getWeeklyWiseRanking(Long userId);

    void calculateAndSaveWeeklyWiseRanking();

}
