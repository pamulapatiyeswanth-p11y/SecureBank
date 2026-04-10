package com.securebank.api_gateway.filter;

import com.securebank.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
//@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config>{
    private final JwtUtil jwtUtil;
    public AuthFilter(JwtUtil jwtUtil){
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(AuthFilter.Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            log.debug("Gateway processing request: {}", path);
            //Get Authorization Header
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            //Check if header is null or doesn't start with bearer
            if (authHeader == null || !authHeader.startsWith("Bearer")) {
                log.warn("Missing or invalid Authorization header for: {}", path);
                return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
            }
            //Extract Header
            String token = authHeader.substring(7);
            try {
                //Validate token and extract claims
                assert jwtUtil != null;
                Claims claims = jwtUtil.validateTokenAndGetClaims(token);
                // 5. Forward user info as headers to downstream services.
                // Services can read these without needing to parse JWT themselves
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r
                                .header("X-User-Id", claims.get("userId", Long.class).toString())
                                .header("X-User-Email", claims.getSubject())
                                .header("X-User-Role", claims.get("role", String.class))
                        ).build();
                log.debug("JWT validated for userId: {}", claims.get("userId"));
                return chain.filter(modifiedExchange);

            } catch (JwtException e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
                return unauthorizedResponse(exchange, "Invalid or expired token");
            }
        };
    }
//─ Helper — builds a clean 401 response ─────────────────────
private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String body = """
                "Success" : false,
                "message" : "%s",
                "data" : null
                """.formatted(message);
        var buffer = exchange.getResponse()
                .bufferFactory().wrap(body.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));


}
    public static class Config {
        // Empty — no config needed for now
        // You can add per-route config here later e.g. roles required
    }

}
