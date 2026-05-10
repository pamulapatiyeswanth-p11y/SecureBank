package com.securebank.user_service.dto.request;

import com.securebank.user_service.entity.OtpPurpose;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendOtpRequest {

    @NotNull(message = "Purpose is required")
    private OtpPurpose purpose;

    // Required for CHANGE_EMAIL and CHANGE_PHONE
    // Null for CHANGE_PASSWORD
    private String pendingValue;
}
