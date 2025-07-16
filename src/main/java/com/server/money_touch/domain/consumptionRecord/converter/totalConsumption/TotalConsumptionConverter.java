package com.server.money_touch.domain.consumptionRecord.converter.totalConsumption;

import com.server.money_touch.domain.consumptionRecord.entity.TotalConsumption;
import com.server.money_touch.domain.user.entity.User;

public class TotalConsumptionConverter {

    // 총 소비 엔티티 생성
    public static TotalConsumption toTotalConsumption(User user) {
        return TotalConsumption.builder()
                .user(user)
                .build();
    }
}
