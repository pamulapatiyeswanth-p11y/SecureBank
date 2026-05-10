package com.securebank.accountservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewAccountRequest {
    @NotNull(message = "Decision is required")
    private Boolean approved;       // true = approve, false = reject
    private String remarks;
}
