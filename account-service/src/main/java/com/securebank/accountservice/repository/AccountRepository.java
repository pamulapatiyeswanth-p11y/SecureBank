package com.securebank.accountservice.repository;


import com.securebank.accountservice.entity.Account;
import com.securebank.accountservice.entity.AccountStatus;
import com.securebank.accountservice.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {

    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomerId(Long customerId);
    List<Account> findByAccountType(AccountType accountType);
    List<Account> findByAccountStatus(AccountStatus accountStatus);
    List<Account> findByCustomerIdAndAccountType(Long customerId,AccountType accountType);
    boolean existsByCustomerIdAndAccountType(Long customerId, AccountType accountType);
    boolean existsByAccountNumber(String accountNumber);

}
