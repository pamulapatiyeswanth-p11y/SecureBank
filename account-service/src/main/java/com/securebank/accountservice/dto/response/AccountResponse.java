package com.securebank.accountservice.dto.response;

import com.securebank.accountservice.entity.AccountStatus;
import com.securebank.accountservice.entity.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private Long customerId;
    private String customerName; // Fetched from User Service
    private String customerEmail; // Fetched from User Service
    private AccountType accountType;
    private AccountStatus accountStatus;
    private BigDecimal balance;
    private String IFSCCode;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityDate;


}
