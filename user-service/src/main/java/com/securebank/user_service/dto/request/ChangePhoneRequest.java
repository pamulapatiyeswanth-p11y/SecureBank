package com.securebank.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePhoneRequest {

//    @NotBlank(message = "New phone is required")
//    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
//    private String newPhone;

    @NotBlank(message = "OTP is required")
    private String otpCode;
}
