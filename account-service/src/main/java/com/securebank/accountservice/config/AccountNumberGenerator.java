package com.securebank.accountservice.config;

import com.securebank.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
@RequiredArgsConstructor
public class AccountNumberGenerator {
    private final AccountRepository accountRepository;

    public String generate(){
        // Get total accounts ever created — used as sequence base
        long count = accountRepository.count();
        // Get current year
        String year = String.valueOf(Year.now().getValue());
        // Pad to 6 digits e.g. 1 → 000001, 123 → 000123
        String sequence = String.format("%06d",count);
        String accountNumber = "SCB-" + year + "-"+sequence;
        // Handle collision — if number already exists increment until unique
        while(accountRepository.existsByAccountNumber(accountNumber)){
            count++;
            sequence = String.format("%06d",count);
            accountNumber = "SCB-" + year + "-"+sequence;
        }
        return  accountNumber;
    }

}
