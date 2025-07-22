package com.server.money_touch.domain.home.service;

import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.home.dto.HomeResponse;
import com.server.money_touch.domain.home.entity.WiseRankingHistory;
import com.server.money_touch.domain.home.repository.WiseRankingHistoryRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final UserRepository userRepository;
    private final ConsumptionRecordRepository consumptionRecordRepository;
    private final WiseRankingHistoryRepository wiseRankingHistoryRepository;

    @Override
    @Transactional
    public void calculateAndSaveWeeklyWiseRanking(){

        LocalDate rankingWeekStart = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate rankingWeekEnd = rankingWeekStart.plusDays(6);

        // 1. 지난주 기록에서 유저별 wiseCount 합산
        List<ConsumptionRecord> records = consumptionRecordRepository
                .findAllByIsPublicTrueAndCreatedAtBetween(
                        rankingWeekStart.atStartOfDay(),
                        rankingWeekEnd.atTime(23, 59, 59));

        Map<User, Integer> userWiseCountMap = new HashMap<>();
        for (ConsumptionRecord record : records) {
            userWiseCountMap.merge(record.getUser(), record.getWiseCount(), Integer::sum);
        }

        // 2. 내림차순 정렬, 랭킹 부여
        List<Map.Entry<User, Integer>> sorted = userWiseCountMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .toList();

        int rank = 1;
        for(Map.Entry<User, Integer> entry : sorted){
            User user = entry.getKey();
            int wiseCount = entry.getValue();

            // 이미 해당 주차 랭킹이 존재한다면 스킵 (덮어쓰기 방지)
            boolean alreadyExists = wiseRankingHistoryRepository
                    .findByUserAndRankingWeekStartDate(user, rankingWeekStart)
                    .isPresent();

            if (alreadyExists) {
                continue;
            }

            wiseRankingHistoryRepository.save(WiseRankingHistory.builder()
                    .user(user)
                    .ranking(rank)
                    .rankingWeekStartDate(rankingWeekStart)
                    .wiseCount(wiseCount)
                    .build());
            rank++;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HomeResponse.WiseRankingResponseDTO getWeeklyWiseRanking(Long userId) {

        LocalDate rankingWeekStart = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);

        List<WiseRankingHistory> allRankings = wiseRankingHistoryRepository
                .findAllByRankingWeekStartDateOrderByRankingAsc(rankingWeekStart);

        List<HomeResponse.RankingUserDTO> top10 = new ArrayList<>();
        HomeResponse.MyRankingDTO myRankingDTO = null;

        int currentRank = 1;
        for (WiseRankingHistory history : allRankings) {
            User user = history.getUser();
            int wiseCount = history.getWiseCount();

            if (currentRank <= 10) {
                String rankStatus = getRankStatus(user, currentRank, rankingWeekStart);
                top10.add(new HomeResponse.RankingUserDTO(
                        user.getNickname(),
                        user.getProfileImgUrl(),
                        wiseCount,
                        rankStatus
                ));
            }

            if (user.getId().equals(userId)) {
                myRankingDTO = new HomeResponse.MyRankingDTO(
                        user.getNickname(),
                        user.getProfileImgUrl(),
                        currentRank,
                        wiseCount
                );
            }

            currentRank++;
        }

        if (myRankingDTO == null) {
            // 기록이 없는 경우
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
            myRankingDTO = new HomeResponse.MyRankingDTO(
                    user.getNickname(),
                    user.getProfileImgUrl(),
                    allRankings.size() + 1,
                    0
            );
        }

        return new HomeResponse.WiseRankingResponseDTO(top10, myRankingDTO);
    }

    private String getRankStatus(User user, int currentRank, LocalDate thisWeekStart) {
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);

        return wiseRankingHistoryRepository.findByUserAndRankingWeekStartDate(user, lastWeekStart)
                .map(prev -> {
                    if (currentRank < prev.getRanking()) return "UP";
                    if (currentRank > prev.getRanking()) return "DOWN";
                    return "SAME";
                }).orElse("UP"); // 순위권 밖에 존재하다 순위를 얻게 되면 UP
    }

}


