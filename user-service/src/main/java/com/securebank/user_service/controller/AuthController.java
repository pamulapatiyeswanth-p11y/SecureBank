package com.securebank.user_service.controller;

import com.securebank.user_service.dto.request.LoginRequest;
import com.securebank.user_service.dto.request.RegisterRequest;
import com.securebank.user_service.dto.response.ApiResponse;
import com.securebank.user_service.dto.response.LoginResponse;
import com.securebank.user_service.dto.response.UserResponse;
import com.securebank.user_service.service.impl.AuthServiceImplementation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
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
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request)
    {
        LoginResponse loginResponse = authServiceImpl.login(request);
        return ResponseEntity.ok().body(ApiResponse.success("Login Successful",loginResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails){

        if (userDetails == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No token provided. Please login first."));
        }


        return ResponseEntity.ok()
                .body(ApiResponse.
                        success("Authenticated User ",userDetails.getUsername()));
    }

}
