package com.securebank.accountservice.controller;

import com.securebank.accountservice.config.RoleGuard;
import com.securebank.accountservice.dto.request.OpenAccountRequest;
import com.securebank.accountservice.dto.request.ReviewAccountRequest;
import com.securebank.accountservice.dto.response.AccountResponse;
import com.securebank.accountservice.dto.response.ApiResponse;
import com.securebank.accountservice.entity.AccountType;
import com.securebank.accountservice.service.impl.AccountServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/accounts")
@Slf4j
public class AccountController {

    private final AccountServiceImpl accountService;

    @PostMapping("/open")
    public ResponseEntity<ApiResponse<AccountResponse>> openAccount(
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody OpenAccountRequest request){
        log.info("Entered Open Account controller");
    AccountResponse response = accountService.openAccount(customerId,request);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Account request submitted successfully.",response));
    }


    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(
            @PathVariable @NotBlank String accountNumber)
    {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok().body(ApiResponse.success(
                "Account fetched successfully.",
                response));
    }

    @GetMapping("/my-accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            @RequestHeader("X-User-Id") Long customerId
    ){
        List<AccountResponse> response = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        "Accounts fetched successfully",
                        response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomerAndType(
            @RequestHeader("X-User-Id") Long customerId,
            @RequestParam @NotNull AccountType accountType
            )
    {
        return ResponseEntity.ok().body(
                ApiResponse.success("Accounts fetched successfully",
                 accountService.getAccountsByCustomerIdAndAccountType(customerId,accountType)));

    }
    //Admin, Staff
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("customer/{customerId}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomerId(
            @PathVariable @NotNull Long customerId,
            @RequestHeader("X-User-Role") String role){
        RoleGuard.roleCheck(role,"ADMIN", "STAFF");

        List<AccountResponse> response = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        "Accounts fetched successfully",
                        response));
    }
    // Admin — view all accounts

    @GetMapping("/all-accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts(
            @RequestHeader("X-User-Role") String role){
//       String role = request.getHeader("X-User-Role");
//       log.info("User role is: {}", role);
        RoleGuard.roleCheck(role,"ADMIN");
        return ResponseEntity.ok()
                .body(ApiResponse.success("All accounts fetched successfully",
                        accountService.getAllAccounts()));
    }
    // Staff — view all pending account requests

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getPendingAccounts(
            @RequestHeader("X-User-Role") String role)
    {
        RoleGuard.roleCheck(role,"STAFF");
        return ResponseEntity.ok()
                .body(ApiResponse.success("Pending accounts fetched successfully",
                        accountService.getPendingAccounts()));
    }

    // Staff — review a pending account (approve or reject)
    @PatchMapping("{accountId}/review")
    public ResponseEntity<ApiResponse<AccountResponse>> reviewPendingAccount(
            @PathVariable @NotNull Long accountId,
            @RequestHeader("X-User-Id") Long staffId,
            @Valid @RequestBody ReviewAccountRequest request,
            @RequestHeader("X-User-Role") String role){
        RoleGuard.roleCheck(role,"STAFF");
        return ResponseEntity.ok()
                .body(ApiResponse.success("Account reviewed successfully",
                        accountService.reviewAccount(accountId,staffId,request)
                ));
    }
  // Staff/Admin — freeze account
  @PatchMapping("{accountNumber}/freeze")
  public ResponseEntity<ApiResponse<AccountResponse>> freezeAccount(
          @PathVariable @NotBlank String accountNumber,
          @RequestHeader("X-User-Id") Long staffId,
          @RequestHeader("X-User-Role") String role){
      RoleGuard.roleCheck(role,"STAFF","ADMIN");
      return ResponseEntity.ok()
              .body(ApiResponse.success("Account frozen successfully",
                      accountService.freezeAccount(accountNumber,staffId)
              ));
  }
    // Staff/Admin — unfreeze account
    @PatchMapping("{accountNumber}/close")
    public ResponseEntity<ApiResponse<AccountResponse>> closeAccount(
            @PathVariable @NotBlank String accountNumber,
            @RequestHeader("X-User-Id") Long staffId,
            @RequestHeader("X-User-Role") String role){
        RoleGuard.roleCheck(role,"STAFF","ADMIN");
        return ResponseEntity.ok()
                .body(ApiResponse.success("Account closed successfully",
                        accountService.closeAccount(accountNumber,staffId)
                ));
    }
}
