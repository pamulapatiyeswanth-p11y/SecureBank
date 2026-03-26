package com.securebank.user_service.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.websocket.server.ServerEndpoint;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
