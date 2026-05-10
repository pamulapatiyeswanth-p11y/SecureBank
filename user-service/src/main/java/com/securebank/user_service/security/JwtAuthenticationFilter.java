package com.securebank.user_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull  FilterChain filterChain) throws ServletException, IOException {
        // 1. Get Authorization header
        final String authHeader =  request.getHeader("Authorization");
        // 2. If no token → skip this filter (Spring Security will block if route is protected)
        if(authHeader ==null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        //Extract Token (Remove "Bearer" prefix)
        String jwtToken = authHeader.substring(7);
        try{
            String email = jwtUtil.extractEmail(jwtToken);
            if(email!=null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    if(jwtUtil.isTokenValid(jwtToken,userDetails)){
                     //   This creates a Spring Security Authentication object.
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));  //Adds extra info about the request (like IP address, session ID).Useful for auditing and security checks.
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        //Stores the authentication object in the SecurityContext.
                    }
            }
        }catch (Exception e){
            log.error("JWT authentication failed: {}", e.getMessage());
            // Don't throw — just let the request continue unauthenticated
            // Spring Security will return 401 if the route is protected

        }
        filterChain.doFilter(request,response);



    }
}
