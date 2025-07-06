package com.server.money_touch.domain.budget.enums;

public enum CategoryType {
    DEFAULT("기본 카테고리"),
    CUSTOM("내 카테고리"),
    ROUTINE_CATEGORY("소비 루틴 카테고리");

    private final String description;

    CategoryType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}