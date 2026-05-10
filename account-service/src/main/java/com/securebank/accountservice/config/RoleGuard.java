package com.securebank.accountservice.config;

import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;

public class RoleGuard {
    // Throws AccessDeniedException if role not in allowed list
    public static void roleCheck(String actualRole, String... allowedRoles) {
        if (actualRole == null) {
            throw new com.securebank.accountservice.exception.AccessDeniedException("No role found in request");
        }

        boolean allowed = Arrays.stream(allowedRoles)
                .anyMatch(r -> r.equalsIgnoreCase(actualRole));

        if (!allowed) {
            throw new AccessDeniedException(
                    "Access denied. Required: " + Arrays.toString(allowedRoles)
                            + " but was: " + actualRole);
        }
    }
}
