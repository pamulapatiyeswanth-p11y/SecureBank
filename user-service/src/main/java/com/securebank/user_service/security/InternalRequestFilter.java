package com.securebank.user_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InternalRequestFilter extends OncePerRequestFilter {

    @Value("${internal.secret}")
    private String internalSecret;

    @Override
    protected void doFilterInternal(
           @NotNull HttpServletRequest request,
           @NotNull HttpServletResponse response,
           @NotNull FilterChain filterChain) throws ServletException, IOException {
        String secret = request.getHeader("X-Internal-Secret");
        String userId = request.getHeader("X-User-Id");
        String role   = request.getHeader("X-User-Role");
        String email =  request.getHeader("X-User-Email");

      // If valid internal secret — authenticate the request directly
        // bypassing JWT validation entirely
        if (internalSecret.equals(secret) && userId != null && role != null) {
            UserDetails serviceUserDetails = org.springframework.security.core.userdetails.User
                    .withUsername(email)
                    .password("")
                    .authorities("ROLE_" + role)
                    .build();
            log.debug("Internal service call authenticated for userId: {}", userId);
          // Create authentication so Spring Security treats this as authenticated
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            serviceUserDetails,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
        }
}
