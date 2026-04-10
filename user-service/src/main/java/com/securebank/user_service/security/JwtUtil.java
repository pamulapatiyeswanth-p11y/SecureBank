package com.securebank.user_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
// Extract the secret and expiration from application.yml or application.properties file
    @Value("${jwt.secret}")
    private String secret ;
    @Value("${jwt.expiration}")
    private long expiration;

    // Create a secret key
    public SecretKey getSigningKey(){
        byte[] decodedSecret = Decoders.BASE64.decode(secret); // Converts the Base64 string back into raw bytes.
        return Keys.hmacShaKeyFor(decodedSecret); //Generates a SecretKey suitable for HMAC‑SHA signing.
    }

    // Generate a token
    public String generateToken(UserDetails userDetails, Long userId,String role){

        Map<String,Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("role", role);
        return buildToken(extraClaims,userDetails);

    }
    // Build the token and return it
    public String buildToken(Map<String,Object> extraClaims,UserDetails userDetails){

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // subject = email
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSigningKey())
                .compact();

    }
//    @PostConstruct
//    public void init() {
//        // This prints the ACTUAL secret being used
//        System.out.println("=== JWT SECRET FROM YML ===");
//        System.out.println(secret);
//        System.out.println("Length chars: " + secret.length());
//        byte[] decoded = Decoders.BASE64.decode(secret);
//        System.out.println("Decoded bytes: " + decoded.length);
//        System.out.println("Decoded bits: " + (decoded.length * 8));
//        System.out.println("===========================");
//    }

    //Validate Token
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date()); // Check if expiration time is past the current time
    }
    // Extract claims
    public String extractEmail(String token){
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }
    public String extractRole(String token){
        return extractAllClaims(token).get("role",String.class);
    }
    public Long extractUserId(String token){
        return extractAllClaims(token).get("userId",Long.class);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
