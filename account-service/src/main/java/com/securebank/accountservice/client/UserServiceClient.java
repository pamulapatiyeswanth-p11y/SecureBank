package com.securebank.accountservice.client;

import com.securebank.accountservice.config.FeignConfig;
import com.securebank.accountservice.dto.response.ApiResponse;
import com.securebank.accountservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        configuration = FeignConfig.class)
public interface UserServiceClient {
    @GetMapping("/api/users/{userId}")
    ApiResponse<UserResponse> getUserById(@PathVariable Long userId);
}
