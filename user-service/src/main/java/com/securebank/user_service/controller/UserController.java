package com.securebank.user_service.controller;

import com.securebank.user_service.dto.request.*;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
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
       return ResponseEntity.ok().body(ApiResponse.success("Profile fetched successfully",response));

   }
   @GetMapping("/{userId}")
   public ResponseEntity<ApiResponse<UserResponse>> getUserById(
           @PathVariable Long userId,
           @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
       UserResponse userResponse = userServiceImplementation.getUserByID(userId,userDetails);
       return ResponseEntity.ok().body(ApiResponse.success("User found",userResponse));
   }

   @PostMapping("/me/otp/send")
   public ResponseEntity<ApiResponse<Void>> sendOtp(
           @AuthenticationPrincipal UserDetails userDetails,
           @Valid @RequestBody SendOtpRequest request)
   {
      userServiceImplementation.sendOtp(userDetails.getUsername(),request);
       return ResponseEntity.ok()
               .body(ApiResponse.success("OTP sent to the user successfully. ",null));
   }

   @PatchMapping("/me/email")
   public ResponseEntity<ApiResponse<Void>> changeEmail(
           @AuthenticationPrincipal UserDetails userDetails,
           @Valid @RequestBody ChangeEmailRequest request)
   {
       userServiceImplementation.changeEmail(userDetails.getUsername(),request);
       return ResponseEntity.ok()
               .body(ApiResponse.success("Email updated successfully. ",null));
   }
    @PatchMapping("/me/phone")
    public ResponseEntity<ApiResponse<Void>> changePhoneNumber(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePhoneRequest request)
    {
        userServiceImplementation.changePhone(userDetails.getUsername(),request);
        return ResponseEntity.ok()
                .body(ApiResponse.success("Phone number updated successfully. ",null));
    }

    //--------Staff + Admin--------------------------------------------------------
    @PatchMapping("/{userId}/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request){
        UserResponse response = userServiceImplementation.updateUserProfile(userId,request);
        return ResponseEntity.ok().body(ApiResponse.success(" User profile updated successfully",response));
    }

  //----------Admin only--------------------------------------------------------

  @PostMapping("/staff")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserResponse>> createStaff(
          @Valid @RequestBody CreateStaffRequest request){
        UserResponse response = userServiceImplementation.createStaffUser(request);
      return ResponseEntity.ok().body(ApiResponse.success("Staff account created successfully",response));


  }

  @PatchMapping("/{userId}/role")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
          @PathVariable Long userId,
          @Valid @RequestBody UpdateRoleRequest request){
      UserResponse response = userServiceImplementation.updateUserRole(userId,request);
      return ResponseEntity.ok().body(ApiResponse.success(" User Role updated successfully",response));
  }

  @PatchMapping("/{userId}/activate")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserResponse>> activateUser(
          @PathVariable Long userId){
      userServiceImplementation.activateUser(userId);
      return ResponseEntity.ok().body(ApiResponse.success("User activated successfully",null));
  }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @PathVariable Long userId){
        userServiceImplementation.deactivateUser(userId);
        return ResponseEntity.ok().body(ApiResponse.success("User deactivated successfully",null));
    }

}
