package com.securebank.accountservice.entity;

public enum AccountStatus {
    PENDING, // Waiting for staff approval
    ACTIVE, // Opened and operational after 24 Hrs
    FROZEN, // Temporarily blocked by staff
    DORMANT, // No activity for a defined period of 12 months.
    CLOSED,// Permanently closed
}
//Schedule a batch job to run every weekly once to check for inactive accounts and mark them dormant