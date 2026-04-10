package com.securebank.user_service.service;


import com.securebank.user_service.dto.request.LoginRequest;
import com.securebank.user_service.dto.request.RegisterRequest;
import com.securebank.user_service.dto.response.LoginResponse;
import com.securebank.user_service.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);

}
