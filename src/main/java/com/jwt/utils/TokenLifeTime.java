package com.jwt.utils;

import lombok.Getter;

@Getter
public enum TokenLifeTime {

    ONE_DAY(86400000);

    private final long days;

    TokenLifeTime(long days) {
        this.days = days;
    }

}
