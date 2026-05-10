package com.securebank.accountservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignConfig implements RequestInterceptor {
    @Value("${internal.secret}")
    private String internalSecret;
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes != null){
            String userId = attributes.getRequest()
                    .getHeader("X-User-Id");
            String role = attributes.getRequest()
                    .getHeader("X-User-Role");
            String email = attributes.getRequest()
                    .getHeader("X-User-Email");
            String auth  = attributes.getRequest()
                    .getHeader("Authorization"); // <-- JWT token
            log.info("Feign forwarding headers - UserId: {}, Role: {}", userId, role);
            if (userId != null) requestTemplate.header("X-User-Id", userId);
            if (role != null)   requestTemplate.header("X-User-Role", role);
            if (email != null)  requestTemplate.header("X-User-Email", email);
//            if(auth != null) requestTemplate.header("Authorization",auth);

        }
        else {
            log.warn("FeignConfig: No request attributes found - headers not forwarded");
        }
        requestTemplate.header("X-Internal-Secret", internalSecret);

    }
}
