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
import java.util.*;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final UserRepository userRepository;
    private final ConsumptionRecordRepository consumptionRecordRepository;
    private final WiseRankingHistoryRepository wiseRankingHistoryRepository;

    @Override
    @Transactional
    public void calculateAndSaveWeeklyWiseRanking() {

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
        for (Map.Entry<User, Integer> entry : sorted) {
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


    @Override
    @Transactional(readOnly = true)
    public HomeResponse.ConsumptionStatisticsTopResponseDTO getTopStatistics(User user) {

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfMonth = LocalDateTime.now().plusMonths(1).withDayOfMonth(1).minusNanos(1);

        // 카테고리 별로 금액 합산
        List<Object[]> results = consumptionRecordRepository.findCategorySpendingBetween(user, startOfMonth, endOfMonth);

        // 소비 기록 없다면 빈 리스트&null 반환
        if (results.isEmpty()) {
            return new HomeResponse.ConsumptionStatisticsTopResponseDTO(List.of(), false, 0.0,null);
        }

        // 카테고리 별로 Map 생성하고 총합 계산
        Map<String, Integer> amountMap = new HashMap<>();
        int total = 0;
        for (Object[] row : results) {
            String category = (String) row[0];
            int amount = ((Long) row[1]).intValue();
            amountMap.put(category, amount);
            total += amount;
        }

        // 소비 금액 내림차순 정렬
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(amountMap.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // 상위 5개 카테고리 추출하고 퍼센트 계산(소수점 1자리, 2자리에서 반올림)
        List<HomeResponse.ConsumptionStatisticsDTO> top5 = new ArrayList<>();
        double percentageSum = 0.0;
        int topCount = Math.min(5, sorted.size());
        for (int i = 0; i < topCount; i++) {
            Map.Entry<String, Integer> entry = sorted.get(i);
            double percent = Math.round((entry.getValue() * 1000.0 / total)) / 10.0;
            top5.add(new HomeResponse.ConsumptionStatisticsDTO(entry.getKey(), percent));
            percentageSum += percent;
        }

        // 기타 카테고리 계산
        boolean hasOthers = sorted.size() > 5;
        double othersPercent = 0.0;
        if (hasOthers) {
            othersPercent = Math.round((100.0 - percentageSum) * 10.0) / 10.0;
            percentageSum += othersPercent;
        }

        // 퍼센트 총합이 100% 되도록 오차 보정
        double correction = Math.round((100.0 - percentageSum) * 10.0) / 10.0;
        if (!top5.isEmpty()) {
            HomeResponse.ConsumptionStatisticsDTO maxCategory = Collections.max(
                    top5,
                    Comparator.comparingDouble(HomeResponse.ConsumptionStatisticsDTO::getPercentage));
            maxCategory.setPercentage(Math.round((maxCategory.getPercentage() + correction) * 10.0) / 10.0);
        }

        // 최다 소비 카테고리명
        String mostSpentCategoryName = sorted.get(0).getKey();

        //최종 응답
        return new HomeResponse.ConsumptionStatisticsTopResponseDTO(
                top5,
                hasOthers,
                othersPercent,
                mostSpentCategoryName
        );

    }

    @Override
    @Transactional(readOnly = true)
    public HomeResponse.OtherCategoryStatisticsResponseDTO getOtherStatistics(User user) {

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfMonth = LocalDateTime.now().plusMonths(1).withDayOfMonth(1).minusNanos(1);

        // 카테고리 별 금액 합산
        List<Object[]> results = consumptionRecordRepository.findCategorySpendingBetween(user, startOfMonth, endOfMonth);

        // 소비 기록이 없다면 빈 리스트 반환
        if (results.isEmpty()) {
            return new HomeResponse.OtherCategoryStatisticsResponseDTO(List.of());
        }

        // 카테고리 별로 Map 생성하고 총합 계산
        Map<String, Integer> amountMap = new HashMap<>();
        int total = 0;
        for (Object[] row : results) {
            String category = (String) row[0];
            int amount = ((Long) row[1]).intValue();
            amountMap.put(category, amount);
            total += amount;
        }

        // 소비 금액 내림차순 정렬
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(amountMap.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // 상위 5개를 제외한 나머지를 그외로 간주
        int topCount = Math.min(5, sorted.size());
        List<Map.Entry<String, Integer>> others = sorted.subList(topCount, sorted.size());

        // 그외 항목이 없다면 빈 리스트 반환
        if (others.isEmpty()) {
            return new HomeResponse.OtherCategoryStatisticsResponseDTO(List.of());
        }

        // 그외 카테고리 개별 항목 퍼센트 계산 (소수점 1자리 반올림)
        List<HomeResponse.ConsumptionStatisticsDTO> othersList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : others) {
            double percent = Math.round((entry.getValue() * 1000.0 / total)) / 10.0;
            othersList.add(new HomeResponse.ConsumptionStatisticsDTO(entry.getKey(), percent));
        }

        return new HomeResponse.OtherCategoryStatisticsResponseDTO(othersList);
    }
}

