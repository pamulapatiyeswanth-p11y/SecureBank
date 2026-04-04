package com.securebank.user_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileRequest {
    @NotBlank(message= "First name cannot be empty")
    private String firstName;
    @NotBlank(message = "Last name cannot be empty")
    private String lastName;
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$",message = "Phone number must be 10 digits")
    private String phoneNumber;
}
