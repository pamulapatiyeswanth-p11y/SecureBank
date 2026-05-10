package com.securebank.user_service.dto.request;

import com.securebank.user_service.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateRoleRequest {
    @NotNull(message = "Role is required")
    private Role role;
}
