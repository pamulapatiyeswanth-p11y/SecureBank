package com.securebank.user_service.entity;

public enum Role {
    CUSTOMER,
    STAFF,
    LOAN_OFFICER,
    ADMIN,
    UNDERWRITER,
    CASE_MANAGER ;
    
    @Override
    public String toString() {
        return this.name();
    }
}
