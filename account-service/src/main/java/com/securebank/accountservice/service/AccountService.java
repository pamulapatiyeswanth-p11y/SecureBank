package com.securebank.accountservice.service;

import com.securebank.accountservice.dto.request.OpenAccountRequest;
import com.securebank.accountservice.dto.request.ReviewAccountRequest;
import com.securebank.accountservice.dto.response.AccountResponse;
import com.securebank.accountservice.entity.AccountStatus;
import com.securebank.accountservice.entity.AccountType;

import java.util.List;

public interface AccountService {

    AccountResponse openAccount(Long customerId, OpenAccountRequest request);

    // Get single account by account number
    AccountResponse getAccountByNumber(String accountNumber);

    // Get all accounts for a customer
    List<AccountResponse> getAccountsByCustomerId(Long customerId);

    // Get all accounts for a customer of provided account type
    List<AccountResponse> getAccountsByCustomerIdAndAccountType(Long customerId, AccountType accountType);

    // Get all pending accounts — Staff only
    List<AccountResponse> getPendingAccounts();

    // Get all accounts — Admin only
    List<AccountResponse> getAllAccounts();

    // Update Account Status - Staff / Admin only
    String updateAccountStatus(Long accountId,AccountStatus status);

    // Staff — approve or reject a pending account
    AccountResponse reviewAccount(Long accountId, Long staffId,
                                  ReviewAccountRequest request);

    // Staff/Admin — freeze an active account
    AccountResponse freezeAccount(String accountNumber, Long staffId);

    // Staff/Admin — unfreeze a frozen account
    AccountResponse unfreezeAccount(String accountNumber, Long staffId);

    // Admin — permanently close an account
    AccountResponse closeAccount(String accountNumber, Long staffId);
}
