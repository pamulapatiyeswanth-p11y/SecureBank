package com.securebank.accountservice.entity;

public enum AccountType {
    SAVINGS("01"),
    CHECKING("02"),
    FIXED_DEPOSIT("03"),
    VEHICLE_LOAN("11"),
    HOME_LOAN("15"),
    PERSONAL_LOAN("12");


    private final String suffix;

    AccountType(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }
}
