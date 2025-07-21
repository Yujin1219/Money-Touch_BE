package com.server.money_touch.domain.consumptionRecord.converter.totalConsumption;

import com.server.money_touch.domain.consumptionRecord.entity.TotalConsumption;
import com.server.money_touch.domain.user.entity.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TotalConsumptionConverter {

    // 총 소비 엔티티 생성
    public static TotalConsumption toTotalConsumption(User user) {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return TotalConsumption.builder()
                .user(user)
                .createdMonth(currentMonth)
                .build();
    }
}
