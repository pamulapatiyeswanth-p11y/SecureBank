package com.securebank.user_service.service.impl;

import com.securebank.user_service.dto.request.LoginRequest;
import com.securebank.user_service.dto.request.RegisterRequest;
import com.securebank.user_service.dto.response.LoginResponse;
import com.securebank.user_service.dto.response.UserResponse;
import com.securebank.user_service.entity.Role;
import com.securebank.user_service.entity.User;
import com.securebank.user_service.exception.InvalidCredentialsException;
import com.securebank.user_service.exception.ResourceAlreadyExistsException;
import com.securebank.user_service.repository.UserRepository;
import com.securebank.user_service.security.JwtUtil;
import com.securebank.user_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Time;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImplementation implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    @Override
    public UserResponse register(RegisterRequest request)
    {
        log.info("Registering new user with email: {}", request.getEmail());
        if(userRepository.existsByEmail(request.getEmail())){
            throw new ResourceAlreadyExistsException("Email already registered: "+ request.getEmail());
        }
        if(userRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new ResourceAlreadyExistsException("Phone number already registered: "+ request.getPhoneNumber());
        }

        User user =  User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.CUSTOMER)
                .enabled(true) //When you use Lombok’s @Builder, the generated builder does not automatically apply field initializers.
                .build();
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());
        return mapToUserResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        // load user from DB
        User user = userRepository.findByEmail(
                request.getEmail()).orElseThrow(()->
                new InvalidCredentialsException("Invalid email or password"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtUtil.generateToken(
                userDetails,
                user.getId(),
                user.getRole().name());
        log.info("Login successful for userId: {}", user.getId());
        return LoginResponse.builder()
                        .userId(user.getId())
                        .accessToken(jwtToken)
                        .tokenType("Bearer")
                        .expiresIn(jwtUtil.extractExpiration(jwtToken).getTime()-System.currentTimeMillis())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build();


    }
    // Private helper — maps entity to DTO
    private UserResponse mapToUserResponse(User savedUser) {
        return UserResponse.builder()
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .enabled(savedUser.isEnabled())
                .updatedAt(savedUser.getUpdatedAt())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }


}
