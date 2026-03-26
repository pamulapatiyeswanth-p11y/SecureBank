package com.securebank.user_service.service;

import com.securebank.user_service.dto.request.RegisterRequest;
import com.securebank.user_service.dto.response.UserResponse;
import com.securebank.user_service.entity.Role;
import com.securebank.user_service.entity.User;
import com.securebank.user_service.exception.ResourceAlreadyExistsException;
import com.securebank.user_service.repository.UserRepository;
import com.securebank.user_service.service.impl.AuthService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImplementation implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
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
