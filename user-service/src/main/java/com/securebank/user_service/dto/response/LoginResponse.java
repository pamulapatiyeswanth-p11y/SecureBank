package com.securebank.user_service.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String role;
    private Long expiresIn;        // milliseconds
}