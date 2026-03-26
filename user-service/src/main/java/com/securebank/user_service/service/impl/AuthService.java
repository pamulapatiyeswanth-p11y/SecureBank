package com.securebank.user_service.service.impl;


import com.securebank.user_service.dto.request.RegisterRequest;
import com.securebank.user_service.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

}
