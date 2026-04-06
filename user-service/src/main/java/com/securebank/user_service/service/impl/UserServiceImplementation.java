package com.securebank.user_service.service.impl;

import com.securebank.user_service.dto.request.*;
import com.securebank.user_service.dto.response.UserResponse;
import com.securebank.user_service.entity.OtpPurpose;
import com.securebank.user_service.entity.Role;
import com.securebank.user_service.entity.User;
import com.securebank.user_service.exception.BadRequestException;
import com.securebank.user_service.exception.ResourceAlreadyExistsException;
import com.securebank.user_service.exception.ResourceNotFoundException;
import com.securebank.user_service.repository.UserRepository;
import com.securebank.user_service.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpServiceImplementation otpService;

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream().map(this::mapToUserResponse).toList();

    }


    @Override
    public UserResponse createStaffUser(CreateStaffRequest request) {
        log.info("Creating staff account with email: {}", request.getEmail());
        if(request.getRole() == Role.CUSTOMER || request.getRole() == Role.ADMIN){
            throw new IllegalArgumentException("Can only create STAFF or " +
                    "LOAN_OFFICER or UNDERWRITER or CASE_MANAGER accounts through this endpoint");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new ResourceAlreadyExistsException("Email already registered: "+ request.getEmail());
        }
        if(userRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new ResourceAlreadyExistsException("Phone number already registered: "+request.getPhoneNumber());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Staff account created with id: {}", savedUser.getId());

        return mapToUserResponse(savedUser);
    }

    @Override
    public void deactivateUser(Long userId) {
        log.info("Deactivating user with id: {}", userId);
        User user = findByUserId(userId);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void activateUser(Long userId) {

        User user = findByUserId(userId);
        if(user.isEnabled()){
            throw new IllegalArgumentException("User is already enabled");
        }
        log.info("Re-activating user with id: {}", userId);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void sendOtp(String email, SendOtpRequest request) {
        User user = findByUserEmail(email);
        if ((request.getPurpose() != OtpPurpose.CHANGE_PASSWORD)
                && (request.getPendingValue() == null || request.getPendingValue().isBlank())) {
            throw new BadRequestException("New value is required for " + request.getPurpose());
        }
        if (request.getPurpose() == OtpPurpose.CHANGE_PHONE) {

            // Validate format
            if (!request.getPendingValue().matches("^[0-9]{10}$")) {
                throw new BadRequestException(
                        "New phone must be 10 digits");
            }
            if(userRepository.existsByPhoneNumber(request.getPendingValue())){
                throw new ResourceAlreadyExistsException("Phone number is already in use: "+ request.getPendingValue() +
                        ". Please provide a different one.");
            }
        }
        if (request.getPurpose() == OtpPurpose.CHANGE_EMAIL
                && userRepository.existsByEmail(request.getPendingValue())) {
            throw new ResourceAlreadyExistsException("Email is already in use: "+ request.getPendingValue() +
                    ". Please provide a different one.");
        }

            OtpPurpose otpPurpose = OtpPurpose.valueOf(request.getPurpose().name().toUpperCase());
            otpService.sendOtp(user.getId(), otpPurpose, request.getPendingValue());

    }
    @Override
    @Transactional
    public void changeEmail(String email, ChangeEmailRequest request) {
        User user = findByUserEmail(email);
        // Validate OTP before applying change
        String newEmail = otpService.validateOtp(user.getId(),OtpPurpose.CHANGE_EMAIL,request.getOtpCode());
        //Double check if the new email is already in use
        if(userRepository.existsByEmail(newEmail)){
            throw new ResourceAlreadyExistsException("Email is already in use: "+ newEmail +
                    ". Please provide a different one.");
        }
        user.setEmail(newEmail);
        userRepository.save(user);
        log.info("Email changed successfully for userId: {}", user.getId());
    }

    @Override
    @Transactional
    public void changePhone(String email, ChangePhoneRequest request) {
        User user = findByUserEmail(email);
        // Validate OTP before applying change
        String newPhone = otpService.validateOtp(user.getId(),OtpPurpose.CHANGE_PHONE,request.getOtpCode());
        //Double check if the new phone number is already in use
        if(userRepository.existsByPhoneNumber(newPhone)){
            throw new ResourceAlreadyExistsException("Phone number is already in use: "+newPhone +
                    ". Please provide a different one.");
        }
        user.setPhoneNumber(newPhone);
        userRepository.save(user);
        log.info("Phone number changed successfully for userId: {}", user.getId());
    }

    @Override
    public UserResponse getUserByID(Long userId, UserDetails userDetails) throws AccessDeniedException {
        // Load the requesting user once
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + userDetails.getUsername()));

        // Extract role in a safe way
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");
        // Authorization check: customers can only view their own profile
        if ("ROLE_CUSTOMER".equals(role) && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to view other users' profiles");
        }
        return mapToUserResponse(findByUserId(userId));
    }

    @Override
    public UserResponse getMyProfile(String email) {
        log.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with email: "+ email));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse updateMyProfile(String email, UpdateProfileRequest request) {
        log.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        //Provided a new mobile number but checks if it already exists for another account
        if (!user.getPhoneNumber().equals(request.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Phone number is already in use" + request.getPhoneNumber());

        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        return mapToUserResponse(userRepository.save(user));
    }

    @Override
    public void changeMyPassword(String email, ChangePasswordRequest request) {

    }

    // Update staff's profile
    @Override
    public UserResponse updateUserProfile(Long userId, UpdateProfileRequest request) {

        User user = findByUserId(userId);
        //Provided a new mobile number but checks if it already exists for another user
        if (!user.getPhoneNumber().equals(request.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Phone number is already in use" + request.getPhoneNumber());

        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        return mapToUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUserRole(Long userId, UpdateRoleRequest request) {
            User user = findByUserId(userId);
            log.info("Updating role to {} for userId: {}",request.getRole(),userId);
            user.setRole(request.getRole());
            return mapToUserResponse(userRepository.save(user));

    }

// Private utility methods

    private User findByUserId(Long userId){
        log.info("Fetching user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with ID: "+ userId));
        return user;
    }

    private User findByUserEmail(String email){
        log.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with email: "+ email));
        return user;
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .enabled(user.isEnabled())
                .updatedAt(user.getUpdatedAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
