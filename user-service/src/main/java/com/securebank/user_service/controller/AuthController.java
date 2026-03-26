package com.securebank.user_service.controller;

import com.securebank.user_service.dto.request.RegisterRequest;
import com.securebank.user_service.dto.response.ApiResponse;
import com.securebank.user_service.dto.response.UserResponse;
import com.securebank.user_service.service.AuthServiceImplementation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.memory.UserAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImplementation authServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request){
      UserResponse userResponse =  authServiceImpl.register(request);
      return ResponseEntity
              .ok()
              .body(ApiResponse.success("User registered successfully",userResponse));
    }
}
