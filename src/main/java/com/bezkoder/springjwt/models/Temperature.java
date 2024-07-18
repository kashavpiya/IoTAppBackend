package com.bezkoder.springjwt.models;

public enum Temperature {
    LOW(85),
    MEDIUM(95),
    HIGH(100);

    private int temp;

    Temperature(int temp) {
        this.temp = temp;
    }
}
