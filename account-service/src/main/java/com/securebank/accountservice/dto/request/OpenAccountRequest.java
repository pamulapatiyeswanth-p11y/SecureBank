package com.securebank.accountservice.dto.request;

import com.securebank.accountservice.entity.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpenAccountRequest {
    @NotNull(message = "Account type is required")
    private AccountType accountType;

    // Optional — customer can write why they want this account
    private String remarks;
}
