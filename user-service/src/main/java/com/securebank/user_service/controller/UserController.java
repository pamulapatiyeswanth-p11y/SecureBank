package com.securebank.user_service.controller;

import com.securebank.user_service.dto.request.UpdateProfileRequest;
import com.securebank.user_service.dto.response.ApiResponse;
import com.securebank.user_service.dto.response.UserResponse;
import com.securebank.user_service.service.impl.UserServiceImplementation;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImplementation userServiceImplementation;

    // ─── Any authenticated user ─────────────────────────────────────
    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request)
    {
       UserResponse response = userServiceImplementation.updateMyProfile(userDetails.getUsername(),request);
       return ResponseEntity.ok().body(ApiResponse.success("profile updated successfully",response));

    }
   @GetMapping("/me/profile")
   public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
           @AuthenticationPrincipal UserDetails userDetails)
   {
       UserResponse response = userServiceImplementation.getMyProfile(userDetails.getUsername());
       return ResponseEntity.ok().body(ApiResponse.success("profile updated successfully",response));

   }
   @GetMapping("/{userId}")
   public ResponseEntity<ApiResponse<UserResponse>> getUserById(
           @PathVariable Long userId,
           @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
       UserResponse userResponse = userServiceImplementation.getUserByID(userId,userDetails);
       return ResponseEntity.ok().body(ApiResponse.success("User found",userResponse));
   }
    //Staff + Admin
    @PatchMapping("/{userId}/profile")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UpdateProfileRequest request){
        UserResponse response = userServiceImplementation.updateUserProfile(userId,request);
        return ResponseEntity.ok().body(ApiResponse.success(" User profile updated successfully",response));
    }



}
