package com.securebank.user_service.service;

import com.securebank.user_service.dto.request.*;
import com.securebank.user_service.dto.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface UserService {
     // Admin only
    List<UserResponse> getAllUsers();
    UserResponse createStaffUser(CreateStaffRequest request);
    void deactivateUser(Long userId);
    void activateUser(Long userId);
    void sendOtp(String email, SendOtpRequest request);
    void changeEmail(String email, ChangeEmailRequest request);
    void changePhone(String email, ChangePhoneRequest request);
    //User + staff
    UserResponse getUserByID(Long userID, UserDetails userDetails) throws AccessDeniedException;
    UserResponse getMyProfile(String email);
    // Any authenticated user — update their own profile
    UserResponse updateMyProfile(String email, UpdateProfileRequest request);
    // Any authenticated user — change their own password
    void changeMyPassword(String email, ChangePasswordRequest request);
    // Admin/Staff — update any user's profile
    UserResponse updateUserProfile(Long userId, UpdateProfileRequest request);
    // Super Admin only — change a user's role
    UserResponse updateUserRole(Long userId, UpdateRoleRequest request);




}
