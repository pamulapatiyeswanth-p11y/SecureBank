package com.securebank.accountservice.service.impl;

import com.securebank.accountservice.client.UserServiceClient;
import com.securebank.accountservice.config.AccountNumberGenerator;
import com.securebank.accountservice.dto.request.OpenAccountRequest;
import com.securebank.accountservice.dto.request.ReviewAccountRequest;
import com.securebank.accountservice.dto.response.AccountResponse;
import com.securebank.accountservice.dto.response.ApiResponse;
import com.securebank.accountservice.dto.response.UserResponse;
import com.securebank.accountservice.entity.Account;
import com.securebank.accountservice.entity.AccountStatus;
import com.securebank.accountservice.entity.AccountType;
import com.securebank.accountservice.exception.BadRequestException;
import com.securebank.accountservice.exception.InvalidAccountStatusException;
import com.securebank.accountservice.exception.ResourceNotFoundException;
import com.securebank.accountservice.exception.ServiceUnavailableException;
import com.securebank.accountservice.repository.AccountRepository;
import com.securebank.accountservice.service.AccountService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepo;
    private final UserServiceClient userServiceClient;
    private final AccountNumberGenerator accountNumberGenerator;

    @Override
    public AccountResponse openAccount(Long customerId, OpenAccountRequest request) {
        log.info("Opening account for customerId: {} type: {}",
                customerId, request.getAccountType());
        //  1. Verify customer exists in User Service via Feign
       UserResponse customer = fetchCustomer(customerId);
       log.info("Customers Role:{}", customer.getRole());
        // 2. Verify the user is actually a CUSTOMER role
        if(!customer.getRole().equals("CUSTOMER")){
            log.info("User is not Customer but a {}",
                    customer.getRole());
            throw new BadRequestException("User must be a customer to open an account.");
        }
        // 3. Check customer doesn't already have this account type
        if(accountRepo.existsByCustomerIdAndAccountType(customerId,request.getAccountType()))
        {
            throw new BadRequestException("User already have a " +
                    request.getAccountType().name() + " account");

        }
        // 4. Set minimum balance based on account type
        BigDecimal minimumAccountBalance = getMinimumBalanceForAccountType(request.getAccountType());
        // 5. Build account — status PENDING until staff approves
        Account account = Account.builder()
                .accountNumber(UUID.randomUUID().toString())
                .customerId(customerId)
                .accountType(request.getAccountType())
                .accountStatus(AccountStatus.PENDING)
                .balance(BigDecimal.ZERO)
                .minimumBalance(minimumAccountBalance)
                .IFSCCode(null)
                .remarks(request.getRemarks())
                .build();
        Account accountSaved = accountRepo.save(account);
        log.info("Account request created with id: {}", accountSaved.getId());
        return maptoAccountResponse(accountSaved,customer);
    }

    @Override
    public AccountResponse getAccountByNumber(String accountNumber) {
            Account account = accountRepo.findByAccountNumber(accountNumber)
                    .orElseThrow(()-> new ResourceNotFoundException(
                            "Account not found with Number: " + accountNumber));
            UserResponse response = fetchCustomer(account.getCustomerId());
            return maptoAccountResponse(account,response);


    }

    @Override
    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        UserResponse customer = fetchCustomer(customerId);
        List<Account> accounts = accountRepo.findByCustomerId(customerId);
        return accounts.stream().map(
                account -> maptoAccountResponse(account,customer))
                .toList();
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerIdAndAccountType(Long customerId, AccountType accountType) {
        UserResponse customer = fetchCustomer(customerId);
        List<Account> accounts = accountRepo.findByCustomerIdAndAccountType(customerId,accountType);
        return accounts.stream().map(
                        account -> maptoAccountResponse(account,customer))
                .toList();
    }

    @Override
    public List<AccountResponse> getPendingAccounts() {
        List<Account> pendingAccounts = accountRepo.findByAccountStatus(AccountStatus.PENDING);
        return pendingAccounts.stream()
                .map(
                        account ->
                                maptoAccountResponse(
                                        account,
                                        fetchCustomer(account.getCustomerId())))
                .toList();
    }

    @Override
    public List<AccountResponse> getAllAccounts() {
        List<Account> accounts = accountRepo.findAll();
        return accounts.stream()
                .map(account ->
                        maptoAccountResponse(
                                account,
                                fetchCustomer(account.getCustomerId())))
                .toList();
    }

    @Override
    public String updateAccountStatus(Long accountId,AccountStatus status) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found with Id: "+accountId));
        account.setAccountStatus(status);
        accountRepo.save(account);
        return "Account status updated to: "+ status;
    }

    @Override
    public AccountResponse reviewAccount(Long accountId, Long staffId, ReviewAccountRequest request) {
        log.info("Reviewing accountId: {}, by staffId: {}, decision: {} ",
                accountId,staffId,request.getApproved());
        // 1. Find the account
        Account account = accountRepo.findById(accountId).
                orElseThrow(() -> new ResourceNotFoundException("Account not found with Id: "+accountId));
        // 2. Can only review PENDING accounts
        if(account.getAccountStatus()!= AccountStatus.PENDING){
            throw new BadRequestException("Only pending accounts can be reviewed." +
                    "Current account status for id: "+ account.getId() + " is "+ account.getAccountStatus());
        }
        // 3. Verify staff exists in User Service
        UserResponse staff = fetchCustomer(staffId);
        // ── APPROVE ──────────────────────────────────────────────
        if(request.getApproved()){
            String accountNumber = accountNumberGenerator.generate();
            account.setAccountNumber(accountNumber);
            account.setAccountStatus(AccountStatus.ACTIVE);
            account.setApprovedBy(staffId);
            account.setApprovedAt(LocalDateTime.now());
            account.setRemarks(request.getRemarks()!=null ?
                    request.getRemarks() : "Account approved");
            log.info("Account is approved. Account number generated "+ accountNumber);
        }
        else{
            // ── REJECT ───────────────────────────────────────────────
            if (request.getRemarks() == null || request.getRemarks().isBlank()) {
                throw new BadRequestException(
                        "Remarks are required when rejecting an account");
            }

            account.setAccountStatus(AccountStatus.CLOSED);
            account.setApprovedBy(staffId);
            account.setApprovedAt(LocalDateTime.now());
            account.setRemarks(request.getRemarks());
            log.info("Account rejected. Reason: {}", request.getRemarks());
        }
        Account saved = accountRepo.save(account);
        UserResponse customer = fetchCustomer(saved.getCustomerId());
        return maptoAccountResponse(saved,customer);

    }

    @Override
    public AccountResponse freezeAccount(String accountNumber, Long staffId) {
        log.info("Freezing account: {} by staffId: {}", accountNumber, staffId);
        Account account = findActiveAccount(accountNumber);
        if(account.getAccountStatus()== AccountStatus.FROZEN){
            throw new BadRequestException("Account with number "+ accountNumber +" is already frozen");
        }
        account.setAccountStatus(AccountStatus.FROZEN);
        account.setRemarks("Account frozen by staffId " +staffId);
        return maptoAccountResponse(accountRepo.save(account),
                fetchCustomer(account.getCustomerId()));
    }

    @Override
    public AccountResponse unfreezeAccount(String accountNumber, Long staffId) {
        log.info("Unfreezing account: {} by staffId: {}", accountNumber, staffId);
        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found with account number "+
                        accountNumber));
        if(account.getAccountStatus()!= AccountStatus.FROZEN){
            throw new BadRequestException("Only frozen accounts can be unfrozen ");
        }
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setRemarks("Account unfrozen by staffId " +staffId);
        return maptoAccountResponse(accountRepo.save(account),
                fetchCustomer(account.getCustomerId()));
    }

    @Override
    public AccountResponse closeAccount(String accountNumber, Long staffId) {
        log.info("Closing account: {} by staffId: {}", accountNumber, staffId);
        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found with account number "+
                        accountNumber));
        if(account.getAccountStatus()!= AccountStatus.CLOSED){
            throw new BadRequestException("Only frozen accounts can be unfrozen ");
        }
        // Cannot close account with remaining balance
        if(account.getBalance().compareTo(BigDecimal.ZERO)>0){
            throw new BadRequestException("Cannot close an account with remaining balance. " +
                    "Please withdraw the remaining funds to close the account.");
        }
        account.setAccountStatus(AccountStatus.CLOSED);
        account.setRemarks("Account closed by staffId " +staffId);
        return maptoAccountResponse(accountRepo.save(account),
                fetchCustomer(account.getCustomerId()));
    }


    // ─── Private Helpers ───────────────────────────────────────────
    private UserResponse fetchCustomer(Long customerId) {
        try{
                ApiResponse<UserResponse> response =
                        userServiceClient.getUserById(customerId);
            log.info("Called for user Id {}",customerId);
            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException(
                        "Customer not found with id: " + customerId);
            }
            return response.getData();

        } catch (FeignException.NotFound ex){
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + customerId);
        } catch (FeignException e){
            log.error("Failed to call User Service: {}", e.getMessage());
            log.error("Feign error status: {}", e.status());
            log.error("Feign error message: {}", e.getMessage());
            log.error("Feign error content: {}", e.contentUTF8());
            throw new ServiceUnavailableException(
                    "User Service is unavailable. Please try again later.");
        }
    }

    private BigDecimal getMinimumBalanceForAccountType(AccountType accountType){
           return switch (accountType){
                case SAVINGS -> new BigDecimal("1000.00");
                case CHECKING -> new BigDecimal("500.00");
                case FIXED_DEPOSIT -> new BigDecimal("10000.00");
               default -> throw new IllegalStateException("Unexpected value: " + accountType);
           };
    }
//Generate Account Numbers
    private String accountNumberGenerator(){
        // Get no of accounts ever created - used as a sequence base.
        long count = accountRepo.count();
        // Get Year value to add to account number
        String year = String.valueOf(Year.now().getValue());
        // Create sequence -  Pad to 6 digits e.g. 1 → 000001, 123 → 000123
        String sequence = String.format("%06d", count);
        // Create account number
        String accountNumber = "SECB-" + year + "-" + sequence;
        // Handle collision — if number already exists increment until unique
        while (accountRepo.existsByAccountNumber(accountNumber)){
            count++;
            sequence = String.format("%06d", count);
            accountNumber = "SECB-" + year + "-" + sequence;

        }
        return accountNumber;
    }
    private Account findActiveAccount(String accountNumber) {
        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountNumber));

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException(
                    "Operation requires an ACTIVE account. " +
                            "Current status: " + account.getAccountStatus());
        }

        return account;
    }


    private AccountResponse maptoAccountResponse(Account account, UserResponse userResponse){
            return AccountResponse.builder()
                    .id(account.getId())
                    .accountStatus(account.getAccountStatus())
                    .customerId(userResponse.getId())
                    .customerEmail(userResponse.getEmail())
                    .customerName(userResponse.getFirstName()+" "+userResponse.getLastName())
                    .accountNumber(account.getAccountNumber())
                    .balance(account.getBalance())
                    .IFSCCode(account.getIFSCCode())
                    .remarks(account.getRemarks())
                    .accountType(account.getAccountType())
                    .createdAt(account.getCreatedAt())
                    .lastActivityDate(account.getLastActivityDate())
                    .build();
    }
}
